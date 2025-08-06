package com.marlonleoner.musicando.domain;

import java.util.Date;

import com.marlonleoner.musicando.domain.enums.MatchStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@ToString(exclude = "room")
@EqualsAndHashCode(callSuper = true, exclude = "room")
@SuperBuilder
@Entity
@Table(name = "tb_match")
public class Match extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private MatchStatus status;

    @Column(nullable = true)
    private String playlist;

    @Column(nullable = true)
    private Integer amount;

    @Column(nullable = true)
    private Integer duration;

    private Date endTime;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;
}
