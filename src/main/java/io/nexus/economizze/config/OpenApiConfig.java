package io.nexus.economizze.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracao do Swagger/OpenAPI.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Define metadados da documentacao da API.
     */
    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Economizze API")
                        .description("API para gestao financeira pessoal e controle de cobrancas de cartoes")
                        .version("v1")
                        .contact(new Contact().name("Economizze").email("suporte@economizze.local"))
                        .license(new License().name("Uso interno")));
    }
}
