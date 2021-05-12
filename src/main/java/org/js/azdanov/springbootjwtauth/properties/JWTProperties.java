package org.js.azdanov.springbootjwtauth.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.time.Duration;

import javax.validation.constraints.NotBlank;

@ConstructorBinding
@ConfigurationProperties(prefix = "jwt")
public class JWTProperties {
    @NotBlank
    private final String secret;
    @NotBlank
    private final Duration expiration;

    public JWTProperties(String secret, Duration expiration) {
        this.secret = secret;
        this.expiration = expiration;
    }

    public String getSecret() {
        return secret;
    }

    public Duration getExpiration() {
        return expiration;
    }
}
