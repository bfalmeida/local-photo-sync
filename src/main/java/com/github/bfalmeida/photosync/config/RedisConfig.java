package com.github.bfalmeida.photosync.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class RedisConfig {
    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${valkey.host:127.0.0.1}")
    private String host;

    @Value("${valkey.port:6379}")
    private int port;

    @Value("${valkey.user:}")
    private String user;

    @Value("${valkey.password:}")
    private String password;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        log.info("Configuring Redis connection factory: host={}, port={}, user={}", host, port, user);
        
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        
        if (user != null && !user.isBlank()) {
            config.setUsername(user);
            log.debug("Redis username set: {}", user);
        }
        
        if (password != null && !password.isBlank()) {
            config.setPassword(password);
            log.debug("Redis password set");
        }

        return new LettuceConnectionFactory(config);
    }

    @Bean
    public StringRedisTemplate redisTemplate() {
        return new StringRedisTemplate(redisConnectionFactory());
    }
}
