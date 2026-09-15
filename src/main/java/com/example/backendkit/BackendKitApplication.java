package com.example.backendkit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BackendKitApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendKitApplication.class, args);
    }
}
