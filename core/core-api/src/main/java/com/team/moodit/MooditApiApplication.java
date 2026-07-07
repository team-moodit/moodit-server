package com.team.moodit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class MooditApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MooditApiApplication.class, args);
    }

}
