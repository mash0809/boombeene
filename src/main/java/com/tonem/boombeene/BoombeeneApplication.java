package com.tonem.boombeene;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BoombeeneApplication {

	static void main(String[] args) {
		SpringApplication.run(BoombeeneApplication.class, args);
	}

}
