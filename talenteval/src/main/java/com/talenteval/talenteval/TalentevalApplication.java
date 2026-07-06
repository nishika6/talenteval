package com.talenteval.talenteval;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TalentevalApplication {

	public static void main(String[] args) {
		SpringApplication.run(TalentevalApplication.class, args);
	}

}
