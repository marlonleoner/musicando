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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor(force = true)
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = "currentMatch")
@EqualsAndHashCode(callSuper = true, exclude = "currentMatch")
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

    private boolean connected;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Player> players;

    @OneToOne
    @JoinColumn(name = "current_match_id")
    private Match currentMatch;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Match> matchs;

    private boolean isStatus(RoomStatus other) {
        return Objects.nonNull(status) && status.equals(other);
    }

    public boolean inGame() {
        return !RoomStatus.WAITING.equals(this.status);
    }

    public boolean isFinished() {
        return isStatus(RoomStatus.FINISHED);
    }

    public boolean validateSecrets(String id, String secret) {
        return Objects.nonNull(this.id) &&
                Objects.nonNull(this.secret) &&
                this.id.equals(id) &&
                this.secret.equals(secret);
    }
}
