package com.marlonleoner.musicando.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@NoArgsConstructor
@MappedSuperclass
@SuperBuilder
@Getter
public abstract class BaseEntity {

    @Id
    @Column(updatable = false, nullable = false)
    protected String id;

    @Column(name = "created_at")
    private Date createdAt;

    @Setter
    @Column(name = "updated_at")
    private Date updatedAt;
}
