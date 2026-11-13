package com.marlonleoner.musicando.service.player;

import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.exception.ObjectNotFoundException;
import com.marlonleoner.musicando.repository.PlayerRepository;
import com.marlonleoner.musicando.util.UUID7;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository repository;

    private Player save(Player player) {
        player.setUpdatedAt(new Date());

        return repository.save(player);
    }

    public Optional<Player> findById(String id) {
        return repository.findById(id);
    }

    public Player getOrException(String id) throws ObjectNotFoundException {
        return findById(id).orElseThrow(() -> new ObjectNotFoundException(Player.class));
    }

    public Player createAndSave(String nickname, Room room) {
        Date now = new Date();

        Player player = Player.builder()
                .id(UUID7.generate())
                .nickname(nickname)
                .room(room)
                .secret(UUID7.generate())
                .createdAt(now)
                .updatedAt(now)
                .build();

        return repository.save(player);
    }

    public void delete(Player player) {
        repository.delete(player);
    }

    /**
     * Garante que a sala tenha um VIP. O VIP atual é mantido enquanto estiver conectado; caso contrário, a coroa passa
     * para o jogador conectado mais antigo. Sem ninguém conectado, o VIP atual é mantido, ou, se a sala não tiver VIP,
     * o jogador mais antigo assume.
     *
     * @param roomCode código da sala
     * @return {@code true} se o VIP mudou
     */
    public boolean ensureOwner(String roomCode) {
        final List<Player> players = repository.findByRoomCodeOrderByCreatedAtAsc(roomCode);
        final Optional<Player> owner = players.stream().filter(Player::isOwner).findFirst();

        if (owner.map(Player::isConnected).orElse(false)) {
            return false;
        }

        Optional<Player> candidate = players.stream().filter(Player::isConnected).findFirst();

        if (candidate.isEmpty() && owner.isEmpty() && !players.isEmpty()) {
            candidate = Optional.of(players.getFirst());
        }

        if (candidate.isEmpty()) {
            return false;
        }

        owner.ifPresent(current -> {
            current.setOwner(false);
            save(current);
        });
        candidate.get().setOwner(true);
        save(candidate.get());

        return true;
    }

    public void onConnect(Player player, String sessionId) {
        player.setConnected(true);
        player.setSessionId(sessionId);
        save(player);
    }

    public void onDisconnect(Player player) {
        player.setConnected(false);
        player.setSessionId(null);
        save(player);
    }

    /**
     * Marca todos os jogadores como desconectados. Usado na inicialização, quando nenhuma conexão WebSocket pode estar
     * ativa.
     *
     * @return quantidade de jogadores atualizados
     */
    public int resetConnections() {
        return repository.resetConnections();
    }
}
