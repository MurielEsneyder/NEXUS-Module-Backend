package com.asmetsalud.nexus.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

// ============================================================
// CONFIGURACIÓN DESHABILITADA - Se usa db2 (PostgreSQL) en su lugar
// ============================================================

// @Configuration
// @EnableTransactionManagement
// @EnableJpaRepositories(
//         basePackages = "com.asmetsalud.nexus.db1.repository",
//         entityManagerFactoryRef = "db1EntityManagerFactory",
//         transactionManagerRef = "db1TransactionManager"
// )
public class Db1Config {

    // Propiedades de conexión de db1 obtenidas del archivo de configuración
    @Value("${spring.datasource.db1.url}")
    private String db1Url;

    @Value("${spring.datasource.db1.username}")
    private String db1Username;

    @Value("${spring.datasource.db1.password}")
    private String db1Password;

    @Value("${spring.datasource.db1.driver-class-name}")
    private String db1DriverClassName;

    // Configuración del DataSource para db1
    // @Primary  // ← Comentado porque db2 es el principal ahora
    @Bean(name = "db1DataSource")
    public DataSource db1DataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(db1DriverClassName);
        dataSource.setUrl(db1Url);
        dataSource.setUsername(db1Username);
        dataSource.setPassword(db1Password);
        return dataSource;
    }

    // Configuración de EntityManagerFactory para db1
    // @Primary  // ← Comentado porque db2 es el principal ahora
    @Bean(name = "db1EntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean db1EntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("db1DataSource") DataSource dataSource) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.Oracle12cDialect");
        properties.put("hibernate.hbm2ddl.auto", "validate");

        return builder
                .dataSource(dataSource)
                .packages("com.asmetsalud.nexus.db1.model")
                .persistenceUnit("db1")
                .properties(properties)
                .build();
    }

    // Configuración de TransactionManager para db1
    @Bean(name = "db1TransactionManager")
    public PlatformTransactionManager db1TransactionManager(
            @Qualifier("db1EntityManagerFactory") LocalContainerEntityManagerFactoryBean db1EntityManagerFactory) {
        return new JpaTransactionManager(db1EntityManagerFactory.getObject());
    }
}