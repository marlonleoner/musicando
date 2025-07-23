package com.marlonleoner.musicando.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.marlonleoner.musicando.domain.Player;
import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.repository.PlayerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository repository;

    public Optional<Player> findById(String id) {
        return repository.findById(UUID.fromString(id));
    }

    public Player createAndSave(String nickname, Room room) {
        Player player = Player.builder()
                .id(UUID.randomUUID().toString())
                .nickname(nickname)
                .room(room)
                .secret(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return repository.save(player);
    }
}
