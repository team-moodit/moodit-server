package com.team.moodit.storage.db.core.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class CoreDatasourceConfig {
    @Bean
    @ConfigurationProperties(prefix = "storage.datasource.core")
    HikariConfig coreHikariConfig() {
        return new HikariConfig();
    }

    @Bean
    HikariDataSource coreDataSource(HikariConfig coreHikariConfig) {
        return new HikariDataSource(coreHikariConfig);
    }
}
