package com.bank.ledger.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ledgerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ledger Payment Orchestration Engine")
                        .description("Bank-grade double-entry ledger and payment processing system")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Talha Yılmaz")
                                .url("https://github.com/talhayilmazc")));
    }
}