package com.acas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AcasBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(AcasBackendApplication.class, args);
    }
}
