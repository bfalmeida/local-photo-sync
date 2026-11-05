package com.github.bfalmeida.photosync.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@ConditionalOnProperty(name = "valkey.enabled", havingValue = "true", matchIfMissing = true)
public class RedisConfig {
    /** Logger for Redis configuration. */
    private static final Logger LOG = LoggerFactory.getLogger(RedisConfig.class);

    /** Redis host. */
    @Value("${valkey.host:127.0.0.1}")
    private String host;

    /** Redis port. */
    @Value("${valkey.port:6379}")
    private int port;

    /** Redis username. */
    @Value("${valkey.user:}")
    private String user;

    /** Redis password. */
    @Value("${valkey.password:}")
    private String password;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        LOG.info("Configuring Redis connection factory: host={}, port={}, user={}", host, port, user);

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);

        if (user != null && !user.isBlank()) {
            config.setUsername(user);
            LOG.debug("Redis username set: {}", user);
        }

        if (password != null && !password.isBlank()) {
            config.setPassword(password);
            LOG.debug("Redis password set");
        }

        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.setShareNativeConnection(false);
        return factory;
    }

    @Bean
    public StringRedisTemplate redisTemplate() {
        return new StringRedisTemplate(redisConnectionFactory());
    }
}