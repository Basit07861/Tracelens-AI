package com.tracelens.system.service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SystemStatusService {

    private final JdbcTemplate jdbcTemplate;

    public SystemStatusService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> getSystemStatus() {

        Integer databaseCheck = jdbcTemplate.queryForObject(
                "SELECT 1",
                Integer.class
        );

        boolean databaseConnected =
                databaseCheck != null && databaseCheck == 1;

        Map<String, Object> status = new LinkedHashMap<>();

        status.put("application", "TraceLens AI");
        status.put(
                "description",
                "AI-powered digital evidence analysis and investigation platform"
        );
        status.put("applicationStatus", "UP");
        status.put(
                "databaseStatus",
                databaseConnected ? "CONNECTED" : "UNAVAILABLE"
        );
        status.put("database", "MySQL");
        status.put("backend", "Spring Boot");
        status.put("javaVersion", System.getProperty("java.version"));
        status.put("checkedAt", Instant.now());

        return status;
    }
}