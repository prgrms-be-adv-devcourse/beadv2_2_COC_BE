package com.coc.modi.rental.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {
	
	@Bean
	public OpenAPI openAPI() {
		
		Server server = new Server();
		server.setUrl("http://localhost:8083");
		
		List<Server> servers = new ArrayList<>();
		servers.add(server);
		
		return new OpenAPI()
				.info(new Info().title("Rental API").version("1.0.0"))
				.servers(servers);
	}
}
