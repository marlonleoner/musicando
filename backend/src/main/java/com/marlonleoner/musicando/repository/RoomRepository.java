package com.marlonleoner.musicando.repository;

import com.marlonleoner.musicando.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {

    Boolean existsByCode(String code);

    Optional<Room> findByCode(String code);
}
