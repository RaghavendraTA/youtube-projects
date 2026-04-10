package com.temkarstudios.parkinglot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ParkinglotApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParkinglotApplication.class, args);
	}

}
