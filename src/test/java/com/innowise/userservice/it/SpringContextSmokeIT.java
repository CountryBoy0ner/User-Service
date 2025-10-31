package com.innowise.userservice.it;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@SpringBootTest
@ActiveProfiles("test")
@Import(ContainersConfig.class)
@AutoConfigureTestDatabase(replace = NONE)
public class SpringContextSmokeIT {

    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired StringRedisTemplate redis;

    @Test
    @DisplayName("Spring context loads, Liquibase applied, Redis reachable")
    void contextLoads() {
        Integer one = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        assertEquals(1, one);
        redis.opsForValue().set("k","v");
        assertEquals("v", redis.opsForValue().get("k"));
    }

}
