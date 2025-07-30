package com.marlonleoner.musicando.domain;

import java.util.List;
import java.util.Objects;

import com.marlonleoner.musicando.domain.enums.RoomStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@Table(name = "tb_room")
public class Room extends BaseEntity {

    @Column(updatable = false, nullable = false, unique = true, length = 6)
    private final String code;

    @Column(updatable = false, nullable = false)
    private final String secret;

    @Enumerated(EnumType.STRING)
    private RoomStatus status;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Player> players;

    @OneToOne(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Game game;

    private boolean isStatus(RoomStatus other) {
        return Objects.nonNull(status) && status.equals(other);
    }

    public boolean inGame() {
        return !RoomStatus.WAITING.equals(this.status);
    }

    public boolean isFinished() {
        return isStatus(RoomStatus.FINISHED);
    }
}
