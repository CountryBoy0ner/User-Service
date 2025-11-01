package com.innowise.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
public class RedisCacheConfig {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory cf) {
        var json = new org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer();
        var defaults = org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(
                        org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer(json)
                )
                .disableCachingNullValues()
                .entryTtl(java.time.Duration.ofMinutes(10));
        return org.springframework.data.redis.cache.RedisCacheManager.builder(cf)
                .cacheDefaults(defaults)
                .build();
    }
}
