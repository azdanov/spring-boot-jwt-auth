package org.js.azdanov.springbootjwtauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("org.js.azdanov.springbootjwtauth.properties")
public class SpringBootJwtAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootJwtAuthApplication.class, args);
    }
}
