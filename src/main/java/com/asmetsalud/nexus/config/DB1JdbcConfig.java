package com.asmetsalud.nexus.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

// @Configuration  // ← COMENTAR ESTA LÍNEA
public class DB1JdbcConfig {
    @Autowired
    @Qualifier("db1DataSource")
    private DataSource db1DataSource;

    @Bean
    public JdbcTemplate db1JdbcTemplate() {
        return new JdbcTemplate(db1DataSource);
    }
}