package com.marlonleoner.musicando.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

import com.marlonleoner.musicando.domain.enums.GameStatus;

@NoArgsConstructor(force = true)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Entity
@Table(name = "tb_game")
public class Game extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private GameStatus status;

    @Column(nullable = true)
    private String playlist;

    @Column(nullable = true)
    private Integer amount;

    @Column(nullable = true)
    private Integer duration;

    private LocalDateTime endTime;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
}
