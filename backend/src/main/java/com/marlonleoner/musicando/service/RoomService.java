package com.marlonleoner.musicando.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.repository.RoomRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository repository;

    public Optional<Room> findById(String id) {
        return repository.findById(UUID.fromString(id));
    }
}
