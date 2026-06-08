package com.team.untitle.storage.db.core.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = "com.team.untitle.storage.db.core")
@EnableJpaRepositories(basePackages = "com.team.untitle.storage.db.core")
class CoreJpaConfig {
}
