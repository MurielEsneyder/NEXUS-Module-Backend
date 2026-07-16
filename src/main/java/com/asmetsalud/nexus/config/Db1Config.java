package com.asmetsalud.nexus.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

// ============================================================
// CONFIGURACIÓN DESHABILITADA - Se usa db2 (PostgreSQL) en su lugar
// ============================================================

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.asmetsalud.nexus.solicitudes.repository", // Paquete de repositorios para PostgreSQL
        entityManagerFactoryRef = "db1EntityManagerFactory",
        transactionManagerRef = "db1TransactionManager"
)
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

    // 1. Configuración del DataSource para db1
    @Primary
    @Bean(name = "db1DataSource")
    public DataSource db1DataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(db1DriverClassName);
        dataSource.setUrl(db1Url);
        dataSource.setUsername(db1Username);
        dataSource.setPassword(db1Password);
        return dataSource;
    }

    // 2. Creador del Builder para evitar el error de Bean faltante
    @Primary
    @Bean(name = "db1EntityManagerFactoryBuilder")
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder(JpaProperties jpaProperties) {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setShowSql(jpaProperties.isShowSql());
        adapter.setDatabasePlatform(jpaProperties.getDatabasePlatform());

        return new EntityManagerFactoryBuilder(adapter, jpaProperties.getProperties(), null);
    }

    // 3. Configuración de EntityManagerFactory para db1
    @Primary
    @Bean(name = "db1EntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean db1EntityManagerFactory(
            @Qualifier("db1EntityManagerFactoryBuilder") EntityManagerFactoryBuilder builder,
            @Qualifier("db1DataSource") DataSource dataSource) {

        Map<String, Object> properties = new HashMap<>();

        // Descomenta esta línea si necesitas que Hibernate cree/actualice las tablas automáticamente
        // properties.put("hibernate.hbm2ddl.auto", "update");

        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

        return builder
                .dataSource(dataSource)
                .packages("com.asmetsalud.nexus.solicitudes.entity") // Paquete de entidades PostgreSQL
                .persistenceUnit("db1")
                .properties(properties)
                .build();
    }

    // 4. Configuración de TransactionManager para db1
    @Primary
    @Bean(name = "db1TransactionManager")
    public PlatformTransactionManager db1TransactionManager(
            @Qualifier("db1EntityManagerFactory") LocalContainerEntityManagerFactoryBean db1EntityManagerFactory) {
        return new JpaTransactionManager(db1EntityManagerFactory.getObject());
    }
}