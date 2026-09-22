package com.hemoflow.hemoflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HemoFlow API")
                        .version("1.0")
                        .description("API REST do sistema HemoFlow - Gestão de doações, estoque e distribuição de hemocomponentes")
                        .contact(new Contact()
                                .name("Equipe HemoFlow")
                                .email("contato@hemoflow.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de Desenvolvimento")
                ));
    }
}
