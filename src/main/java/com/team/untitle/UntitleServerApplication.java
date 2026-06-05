package com.team.untitle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class UntitleServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(UntitleServerApplication.class, args);
    }

}
