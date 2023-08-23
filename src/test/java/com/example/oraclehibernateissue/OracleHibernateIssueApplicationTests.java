package com.example.oraclehibernateissue;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.OracleContainer;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

class OracleHibernateIssueApplicationTests {

    static OracleContainer oracle;

    TimeZone TZ_UTC = TimeZone.getTimeZone(ZoneId.of("Z"));
    TimeZone TZ_EUROPE_BERLIN = TimeZone.getTimeZone(ZoneId.of("Europe/Berlin"));

    Instant TIME_10_00 = LocalDateTime
            .of(2023, 8, 9, 10, 0)
            .atZone(TZ_EUROPE_BERLIN.toZoneId())
            .toInstant();
    Instant TIME_09_00 = TIME_10_00.minus(1, ChronoUnit.HOURS);


    @BeforeAll
    static void setUp() {

        oracle = new OracleContainer("gvenzl/oracle-xe:21-slim-faststart")
                .withDatabaseName("testDB")
                .withUsername("testUser")
                .withPassword("testPassword")
                .withReuse(true);
        oracle.start();
        System.setProperty("spring.datasource.url", oracle.getJdbcUrl());
        System.setProperty("spring.datasource.password", oracle.getPassword());
        System.setProperty("spring.datasource.username", oracle.getUsername());
    }

    @SpringBootApplication
    public static class TestApp {

        @Autowired
        EntityManager em;

        @Transactional
        <T> T doInTx(Function<EntityManager, T> function) {
            return function.apply(em);
        }
    }

    @Test
    void isSuccessful() {
        TimeZone.setDefault(TZ_UTC);

        doRun();
    }

    @Test
    void failsButShouldnt() {
        TimeZone.setDefault(TZ_EUROPE_BERLIN);

        doRun();
    }

    void doRun() {
        try (var ctx = SpringApplication.run(TestApp.class)) {
            var testApp = ctx.getBean(TestApp.class);

            // clear db
            testApp.doInTx(em -> em.createQuery("delete from TestEntity").executeUpdate());

            // persist one entity with time set to 10:00
            testApp.doInTx(em -> {
                TestEntity e1 = new TestEntity();
                e1.setInstant(TIME_10_00);
                em.persist(e1);
                return null;
            });

            // load all entites with time < 09:00 (should be empty)
            List<TestEntity> entitiesWithTimeLessThan09_00 = testApp.doInTx(em -> {
                TypedQuery<TestEntity> query = em.createQuery("select i from TestEntity i where i.instant < :instant", TestEntity.class);
                query.setParameter("instant", TIME_09_00);
                return query.getResultList();
            });

            // depending on the system time we find our entity, which should never happen
            assertThat(entitiesWithTimeLessThan09_00, is(empty()));
        }
    }
}
