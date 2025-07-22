package com.marlonleoner.musicando.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.marlonleoner.musicando.domain.Player;

@Repository
public interface PlayerRepository extends JpaRepository<Player, UUID> {

}
