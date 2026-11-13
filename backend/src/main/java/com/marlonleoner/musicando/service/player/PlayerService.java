package com.marlonleoner.musicando.service.player;

import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.exception.ObjectNotFoundException;
import com.marlonleoner.musicando.repository.PlayerRepository;
import com.marlonleoner.musicando.util.UUID7;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
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
