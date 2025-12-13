package com.coc.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication
public class ModiConfigApplication {
	
	public static void main(String[] args) {
		
		SpringApplication.run(ModiConfigApplication.class, args);
	}
	
}
