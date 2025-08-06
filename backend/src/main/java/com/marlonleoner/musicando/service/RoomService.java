package com.marlonleoner.musicando.service;

import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

import com.marlonleoner.musicando.domain.Room;
import com.marlonleoner.musicando.domain.enums.RoomStatus;
import com.marlonleoner.musicando.domain.exception.ObjectNotFoundException;
import com.marlonleoner.musicando.repository.RoomRepository;
import com.marlonleoner.musicando.util.UUID7;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final static String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final static Integer ROOM_CODE_SIZE = 6;

    private final RoomRepository repository;

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

    public Room createAndSave() {
        Date now = new Date();

        String code = generateUniqueRoomCode();

        Room room = Room.builder()
                .id(UUID7.generate())
                .code(code)
                .secret(UUID7.generate())
                .status(RoomStatus.WAITING)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return repository.save(room);
    }

    private String generateUniqueRoomCode() {
        String code;
        do {
            code = generateRoomCode();
        } while (repository.existsByCode(code));

        return code;
    }

    private String generateRoomCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(ROOM_CODE_SIZE);
        for (int i = 0; i < ROOM_CODE_SIZE; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }

        return sb.toString();
    }
}
