package com.asmetsalud.nexus.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class DB2JdbcConfig {

    @Autowired
    @Qualifier("db2DataSource")
    private DataSource db2DataSource;

    @Bean
    public JdbcTemplate db2JdbcTemplate() {
        return new JdbcTemplate(db2DataSource);
    }
}