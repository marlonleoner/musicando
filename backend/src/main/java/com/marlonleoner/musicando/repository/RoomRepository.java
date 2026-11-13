package com.marlonleoner.musicando.repository;

import com.marlonleoner.musicando.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {

    Boolean existsByCode(String code);

    Optional<Room> findByCode(String code);

    @Transactional
    @Modifying
    @Query("update Room r set r.connected = false, r.hostSessionId = null where r.connected = true or r.hostSessionId is not null")
    int resetConnections();
}
