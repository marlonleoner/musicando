package com.marlonleoner.musicando.service.room;

import com.marlonleoner.musicando.domain.Match;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.enums.RoomStatus;
import com.marlonleoner.musicando.domain.exception.ObjectNotFoundException;
import com.marlonleoner.musicando.repository.RoomRepository;
import com.marlonleoner.musicando.util.UUID7;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Integer ROOM_CODE_SIZE = 6;

    private static final int MAX_CODE_ATTEMPTS = 5;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RoomRepository repository;

    private Room save(Room room) {
        room.setUpdatedAt(new Date());

        return repository.save(room);
    }

    private Room saveAndFlush(Room room) {
        return repository.saveAndFlush(room);
    }

    public List<Room> findAll() {
        return repository.findAll();
    }

    public Optional<Room> findById(String id) {
        return repository.findById(id);
    }

    public Optional<Room> findByCode(String code) {
        return repository.findByCode(code);
    }

    public Room getOrException(String code) throws ObjectNotFoundException {
        return findByCode(code).orElseThrow(() -> new ObjectNotFoundException(Room.class));
    }

    /**
     * Cria uma sala com código único. A verificação prévia evita a maioria das colisões; a constraint {@code unique}
     * do banco cobre a corrida entre duas criações simultâneas com o mesmo código, que é tratada com uma nova tentativa.
     *
     * @return sala criada, aguardando jogadores
     */
    public Room createAndSave() {
        for (int attempt = 1; attempt <= MAX_CODE_ATTEMPTS; attempt++) {
            try {
                return saveAndFlush(buildRoom(generateUniqueRoomCode()));
            } catch (DataIntegrityViolationException ex) {
                log.warn("Room code collision while creating room (attempt {} of {})", attempt, MAX_CODE_ATTEMPTS);
            }
        }

        throw new IllegalStateException("Could not generate a unique room code");
    }

    private Room buildRoom(String code) {
        final Date now = new Date();

        return Room.builder()
                .id(UUID7.generate())
                .code(code)
                .secret(UUID7.generate())
                .status(RoomStatus.WAITING)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private String generateUniqueRoomCode() {
        String code;
        do {
            code = generateRoomCode();
        } while (repository.existsByCode(code));

        return code;
    }

    private String generateRoomCode() {
        StringBuilder sb = new StringBuilder(ROOM_CODE_SIZE);
        for (int i = 0; i < ROOM_CODE_SIZE; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }

        return sb.toString();
    }

    /**
     * Marca o host como conectado. Uma partida pausada pela queda do host é retomada; nos demais estados o status da
     * sala é mantido.
     *
     * @param room      sala do host
     * @param sessionId sessão WebSocket do host
     */
    public void onConnect(Room room, String sessionId) {
        room.setConnected(true);
        room.setHostSessionId(sessionId);

        if (room.isPaused()) {
            room.setStatus(RoomStatus.IN_PROGRESS);
        }

        save(room);
    }

    /**
     * Marca o host como desconectado. Uma partida em andamento é pausada; nos demais estados o status da sala é
     * mantido.
     *
     * @param room sala do host
     */
    public void onDisconnect(Room room) {
        room.setConnected(false);
        room.setHostSessionId(null);

        if (room.isInProgress()) {
            room.setStatus(RoomStatus.PAUSED);
        }

        save(room);
    }

    /**
     * Marca todas as salas como desconectadas. Usado na inicialização, quando nenhuma conexão WebSocket pode estar
     * ativa.
     *
     * @return quantidade de salas atualizadas
     */
    public int resetConnections() {
        return repository.resetConnections();
    }

    /**
     * Encerra a sala. Ela deixa de aceitar jogadores e os clientes voltam para a tela inicial.
     *
     * @param room sala a ser encerrada
     */
    public void close(Room room) {
        room.setStatus(RoomStatus.CLOSED);
        save(room);
    }

    public void attachMatch(Room room, Match match) {
        room.setCurrentMatch(match);
        save(room);
    }

    public void start(Room room) {
        room.setStatus(RoomStatus.IN_PROGRESS);
        save(room);
    }

    /**
     * Partidas que estavam em andamento ou pausadas, para serem retomadas depois de um reinício.
     */
    public List<Room> findInterrupted() {
        return repository.findByStatusIn(List.of(RoomStatus.IN_PROGRESS, RoomStatus.PAUSED));
    }

    /**
     * Salas ainda abertas, sem a TV conectada e sem alterações desde o limite.
     */
    public List<Room> findAbandoned(Date limit) {
        return repository.findByStatusNotAndConnectedFalseAndUpdatedAtBefore(RoomStatus.CLOSED, limit);
    }

    public List<Room> findClosedBefore(Date limit) {
        return repository.findByStatusAndUpdatedAtBefore(RoomStatus.CLOSED, limit);
    }

    /**
     * Apaga a sala com seus jogadores e partidas.
     */
    public void delete(Room room) {
        room.setCurrentMatch(null);
        repository.saveAndFlush(room);
        repository.delete(room);
    }

    public void pause(Room room) {
        room.setStatus(RoomStatus.PAUSED);
        save(room);
    }

    public void finish(Room room) {
        room.setStatus(RoomStatus.FINISHED);
        save(room);
    }

    /**
     * Volta a sala para o lobby depois de uma partida encerrada ("jogar novamente").
     */
    public void reopen(Room room) {
        room.setStatus(RoomStatus.WAITING);
        save(room);
    }
}
