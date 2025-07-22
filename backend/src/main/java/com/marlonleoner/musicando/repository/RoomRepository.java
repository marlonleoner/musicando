package com.marlonleoner.musicando.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.marlonleoner.musicando.domain.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

}
