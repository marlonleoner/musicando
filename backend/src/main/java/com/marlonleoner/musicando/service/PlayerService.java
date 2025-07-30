package com.marlonleoner.musicando.service;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;

import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.repository.PlayerRepository;
import com.marlonleoner.musicando.util.UUID7;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository repository;

    public Optional<Player> findById(String id) {
        return repository.findById(id);
    }

    public Player createAndSave(String nickname, Room room) {
        Player player = Player.builder()
                .id(UUID7.generate())
                .nickname(nickname)
                .room(room)
                .secret(UUID7.generate())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return repository.save(player);
    }
}
