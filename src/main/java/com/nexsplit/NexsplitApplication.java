package com.nexsplit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NexsplitApplication {

	public static void main(String[] args) {
		SpringApplication.run(NexsplitApplication.class, args);
	}

}
