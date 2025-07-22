package com.marlonleoner.musicando.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.marlonleoner.musicando.domain.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {

    Boolean existsByCode(String code);
}
