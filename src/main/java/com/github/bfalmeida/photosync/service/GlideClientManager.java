package com.github.bfalmeida.photosync.service;

import glide.api.GlideClient;
import glide.api.models.configuration.GlideClientConfiguration;
import glide.api.models.configuration.NodeAddress;
import glide.api.models.configuration.ServerCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PreDestroy;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class GlideClientManager {
    private static final Logger log = LoggerFactory.getLogger(GlideClientManager.class);
    private GlideClient client;

    public GlideClientManager(
            @Value("${valkey.host:localhost}") String host,
            @Value("${valkey.port:6379}") int port,
            @Value("${valkey.user:default}") String user,
            @Value("${valkey.password:}") String password) {
        
        try {
            ServerCredentials credentials = ServerCredentials.builder()
                .username(user)
                .password(password)
                .build();

            GlideClientConfiguration config = GlideClientConfiguration.builder()
                .address(NodeAddress.builder().host(host).port(port).build())
                .credentials(credentials)
                .build();
            
            this.client = GlideClient.createClient(config).join();
            log.info("Successfully initialized Valkey Glide Client at {}:{}", host, port);
        } catch (Exception e) {
            log.error("Failed to initialize Valkey Glide Client: {}", e.getMessage());
        }
    }

    public GlideClient getClient() {
        return client;
    }

    public boolean isConnected() {
        if (client == null) return false;
        try {
            return client.ping().join().equalsIgnoreCase("PONG");
        } catch (Exception e) {
            return false;
        }
    }

    @PreDestroy
    public void shutdown() {
        if (client != null) {
            try {
                client.close();
                log.info("Valkey Glide Client shut down successfully.");
            } catch (Exception e) {
                log.error("Error during Glide Client shutdown: {}", e.getMessage());
            }
        }
    }
}
