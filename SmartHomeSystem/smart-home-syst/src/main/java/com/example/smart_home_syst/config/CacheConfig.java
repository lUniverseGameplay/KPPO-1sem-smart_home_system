package com.example.smart_home_syst.config;

import java.util.Arrays;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableCaching

public class CacheConfig {
    @Bean
    CacheManager cacheManager() {
        SimpleCacheManager scm = new SimpleCacheManager();
        scm.setCaches(Arrays.asList(
            new ConcurrentMapCache("devices"),
            new ConcurrentMapCache("device")
        ));
        return scm;
    }
}
