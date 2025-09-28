package com.sameboat.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SameboatBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SameboatBackendApplication.class, args);
    }

}
