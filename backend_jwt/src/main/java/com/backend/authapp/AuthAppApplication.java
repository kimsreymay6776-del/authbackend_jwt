package com.example.authapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AuthAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthAppApplication.class, args);
    }
}
