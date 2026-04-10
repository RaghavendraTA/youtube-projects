package com.configapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ConfigurationAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigurationAppApplication.class, args);
    }
}
