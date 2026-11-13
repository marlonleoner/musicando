package com.marlonleoner.musicando.integration;

import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.dto.socket.SocketCommand;
import com.marlonleoner.musicando.service.player.PlayerService;
import com.marlonleoner.musicando.service.room.RoomService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

/**
 * Protocolo WebSocket de ponta a ponta: autenticação no CONNECT, autorização de inscrições, foto da sala, comandos e
 * substituição de conexões.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SocketProtocolIntegrationTest {

    private static final long TIMEOUT_SECONDS = 5;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    private final List<StompSession> sessions = new ArrayList<>();

    private final BlockingQueue<String> errorFrames = new LinkedBlockingQueue<>();

    @LocalServerPort
    private int port;

    @Autowired
    private RoomService roomService;

    @Autowired
    private PlayerService playerService;

    private WebSocketStompClient client;

    private Room room;

    @BeforeEach
    void setUp() {
        client = new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new CompositeMessageConverter(List.of(new StringMessageConverter(), new JacksonJsonMessageConverter())));
        room = roomService.createAndSave();
    }

    @AfterEach
    void tearDown() {
        sessions.stream().filter(StompSession::isConnected).forEach(StompSession::disconnect);
        client.stop();
    }

    @Test
    void shouldRejectConnectionWhenCredentialsAreInvalid() throws InterruptedException {
        final CompletableFuture<StompSession> future = connectAsync("host", room.getCode(), room.getId(), "wrong-secret");

        assertThatThrownBy(() -> future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isInstanceOfAny(ExecutionException.class, TimeoutException.class);
        assertThat(errorFrames.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isEqualTo("INVALID_CREDENTIALS");
    }

    @Test
    void shouldReplyRoomSnapshotWhenHostSubscribesToSnapshot() throws Exception {
        final StompSession host = connect("host", room.getCode(), room.getId(), room.getSecret());

        final JsonNode snapshot = poll(subscribe(host, "/musicando/room"));

        assertThat(snapshot.path("type").asString()).isEqualTo("ROOM_STATE");
        assertThat(snapshot.path("reason").path("type").asString()).isEqualTo("SNAPSHOT");
        assertThat(snapshot.path("payload").path("code").asString()).isEqualTo(room.getCode());
        assertThat(snapshot.path("payload").path("connected").asBoolean()).isTrue();
        assertThat(snapshot.path("payload").has("secret")).isFalse();
    }

    @Test
    void shouldAnswerCommandsAndReportErrorsToSenderWhenHostSendsCommands() throws Exception {
        final StompSession host = connect("host", room.getCode(), room.getId(), room.getSecret());
        final BlockingQueue<JsonNode> events = subscribe(host, "/user/queue/events");
        final BlockingQueue<JsonNode> errors = subscribe(host, "/user/queue/errors");

        host.send("/musicando/command", new SocketCommand("GET_ROOM_STATE", null));
        host.send("/musicando/command", new SocketCommand("UNKNOWN", null));

        assertThat(poll(events).path("type").asString()).isEqualTo("ROOM_STATE");
        final JsonNode error = poll(errors);
        assertThat(error.path("type").asString()).isEqualTo("ERROR");
        assertThat(error.path("payload").path("code").asString()).isEqualTo("INVALID_COMMAND");
    }

    @Test
    void shouldNotifyRoomWhenPlayerConnects() throws Exception {
        final StompSession host = connect("host", room.getCode(), room.getId(), room.getSecret());
        final BlockingQueue<JsonNode> roomEvents = subscribe(host, "/topic/room/" + room.getCode());
        final Player player = playerService.createAndSave("Ana", roomService.getOrException(room.getCode()));

        connect("player", room.getCode(), player.getId(), player.getSecret());

        final JsonNode event = pollUntilReason(roomEvents, "PLAYER_CONNECTED");
        assertThat(event.path("reason").path("participantId").asString()).isEqualTo(player.getId());
        assertThat(event.path("payload").path("players").get(0).path("connected").asBoolean()).isTrue();
        assertThat(event.path("payload").path("players").get(0).has("secret")).isFalse();
    }

    @Test
    void shouldRejectWithErrorFrameWhenPlayerSubscribesToHostTopic() throws Exception {
        final Player player = playerService.createAndSave("Ana", roomService.getOrException(room.getCode()));
        final StompSession session = connect("player", room.getCode(), player.getId(), player.getSecret());

        subscribe(session, "/topic/room/" + room.getCode() + "/host");

        assertThat(errorFrames.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isEqualTo("FORBIDDEN");
    }

    @Test
    void shouldCloseOlderConnectionWhenSameHostConnectsAgain() throws Exception {
        final StompSession first = connect("host", room.getCode(), room.getId(), room.getSecret());

        final StompSession second = connect("host", room.getCode(), room.getId(), room.getSecret());

        await().atMost(TIMEOUT_SECONDS, TimeUnit.SECONDS).until(() -> !first.isConnected());
        assertThat(second.isConnected()).isTrue();
        assertThat(roomService.getOrException(room.getCode()).isConnected()).isTrue();
    }

    private StompSession connect(final String role, final String code, final String id, final String secret) throws Exception {
        final StompSession session = connectAsync(role, code, id, secret).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        sessions.add(session);

        return session;
    }

    private CompletableFuture<StompSession> connectAsync(final String role, final String code, final String id, final String secret) {
        final StompHeaders headers = new StompHeaders();
        headers.add("role", role);
        headers.add("code", code);
        headers.add("id", id);
        headers.add("secret", secret);

        return client.connectAsync("ws://localhost:" + port + "/ws", new WebSocketHttpHeaders(), headers, new StompSessionHandlerAdapter() {
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                errorFrames.add(String.valueOf(headers.getFirst("message")));
            }
        });
    }

    private BlockingQueue<JsonNode> subscribe(final StompSession session, final String destination) {
        final BlockingQueue<JsonNode> queue = new LinkedBlockingQueue<>();
        session.subscribe(destination, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                queue.add(jsonMapper.readTree((String) payload));
            }
        });

        return queue;
    }

    private JsonNode poll(final BlockingQueue<JsonNode> queue) throws InterruptedException {
        final JsonNode node = queue.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertThat(node).as("event received within timeout").isNotNull();

        return node;
    }

    private JsonNode pollUntilReason(final BlockingQueue<JsonNode> queue, final String reason) throws InterruptedException {
        JsonNode node = poll(queue);

        while (!reason.equals(node.path("reason").path("type").asString())) {
            node = poll(queue);
        }

        return node;
    }
}
