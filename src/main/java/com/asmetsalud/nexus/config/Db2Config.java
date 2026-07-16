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

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.asmetsalud.nexus.db1.repository",  // ← Repositorios legacy Oracle
        entityManagerFactoryRef = "db2EntityManagerFactory",
        transactionManagerRef = "db2TransactionManager"
)
public class Db2Config {

    @Value("${spring.datasource.db2.url}")
    private String db2Url;

    @Value("${spring.datasource.db2.username}")
    private String db2Username;

    @Value("${spring.datasource.db2.password}")
    private String db2Password;

    @Value("${spring.datasource.db2.driver-class-name}")
    private String db2DriverClassName;

    // 1. Configuración del DataSource para db2
    @Bean(name = "db2DataSource")
    public DataSource db2DataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(db2DriverClassName);
        dataSource.setUrl(db2Url);
        dataSource.setUsername(db2Username);
        dataSource.setPassword(db2Password);
        return dataSource;
    }

    // 2. Creador del Builder para db2 utilizando propiedades oficiales de Spring
    @Bean(name = "db2EntityManagerFactoryBuilder")
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder(JpaProperties jpaProperties) {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setShowSql(jpaProperties.isShowSql());
        adapter.setDatabasePlatform(jpaProperties.getDatabasePlatform());

        return new EntityManagerFactoryBuilder(adapter, jpaProperties.getProperties(), null);
    }

    // 3. Configuración de EntityManagerFactory para db2
    @Bean(name = "db2EntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean db2EntityManagerFactory(
            @Qualifier("db2EntityManagerFactoryBuilder") EntityManagerFactoryBuilder builder,
            @Qualifier("db2DataSource") DataSource dataSource) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "true");

        // <<< CAMBIADO DE "validate" A "update" >>>
        //properties.put("hibernate.hbm2ddl.auto", "update"); // Asegura que se actualicen las tablas en db2

        return builder
                .dataSource(dataSource)
                .packages("com.asmetsalud.nexus.db1.model")  // ← Entidades legacy Oracle
                .persistenceUnit("db2")
                .properties(properties)
                .build();
    }

    // 4. Configuración de TransactionManager para db2
    @Bean(name = "db2TransactionManager")
    public PlatformTransactionManager db2TransactionManager(
            @Qualifier("db2EntityManagerFactory") LocalContainerEntityManagerFactoryBean db2EntityManagerFactory) {
        return new JpaTransactionManager(db2EntityManagerFactory.getObject());
    }
}