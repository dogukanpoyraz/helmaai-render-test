package com.backend.helmaaibackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class HelmaaibackendApplication { // ← name matches run config
    public static void main(String[] args) {
        SpringApplication.run(HelmaaibackendApplication.class, args);
    }
}
