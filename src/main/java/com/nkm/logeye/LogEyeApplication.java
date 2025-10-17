package com.nkm.logeye;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class LogEyeApplication {

	public static void main(String[] args) {
		SpringApplication.run(LogEyeApplication.class, args);
	}

}
