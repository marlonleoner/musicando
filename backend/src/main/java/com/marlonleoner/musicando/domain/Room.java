package com.marlonleoner.musicando.domain;

import java.util.List;

import com.marlonleoner.musicando.domain.enums.RoomStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor(force = true)
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "tb_room")
public class Room {

    @Id
    @Column(updatable = false, nullable = false)
    private final String id;

    @Column(updatable = false, nullable = false, unique = true, length = 6)
    private final String code;

    @Enumerated(EnumType.STRING)
    private RoomStatus status;

    @Column(nullable = true)
    private String playlist;

    @Column(nullable = true)
    private int count;

    @Column(nullable = true)
    private int duration;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Player> players;
}
