package com.marlonleoner.musicando.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor(force = true)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Entity
@Table(name = "tb_player")
public class Player extends BaseEntity {

    private String sessionId;

    @Column(updatable = false, nullable = false)
    private final String secret;

    private String nickname;

    private int score;

    private int roundsWon;

    private boolean owner;

    private boolean connected;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
}
