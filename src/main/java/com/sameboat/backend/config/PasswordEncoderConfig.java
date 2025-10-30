package com.sameboat.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Provides the application-wide {@link PasswordEncoder}. BCrypt is chosen for
 * adaptive hashing so strength can be increased over time.
 * @author ArchILLtect
 */
@Configuration
public class PasswordEncoderConfig {

    /** Exposes a BCrypt encoder bean. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
