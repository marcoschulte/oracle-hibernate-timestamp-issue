package com.example.oraclehibernateissue;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "test_entity")
public class TestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_test")
    @SequenceGenerator(name = "seq_test")
    private Long id;

    @Column(nullable = false, columnDefinition = "TIMESTAMP(6)")
    private Instant instant;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getInstant() {
        return instant;
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }
}