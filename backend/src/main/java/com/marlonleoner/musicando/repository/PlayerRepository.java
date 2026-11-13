package com.marlonleoner.musicando.repository;

import com.marlonleoner.musicando.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, String> {

    @Transactional
    @Modifying
    @Query("update Player p set p.connected = false, p.sessionId = null where p.connected = true or p.sessionId is not null")
    int resetConnections();
}
