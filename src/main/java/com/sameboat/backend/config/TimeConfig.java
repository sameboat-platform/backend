package com.sameboat.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneOffset;

/**
 * Configuration class providing a UTC Clock bean for consistent time handling.
 * @author ArchILLtect
 */
@Configuration
public class TimeConfig {
    @Bean
    public Clock utcClock() { return Clock.system(ZoneOffset.UTC); }
}

