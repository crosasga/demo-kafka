package com.banco.central.demo.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "spring.kafka")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Generated
public class KafkaPropertiesConfig {

    private String bootstrapServers;
    private Consumer consumer;
    private Producer producer;

    @Data
    public static class Consumer {
        private String groupId;
        private final String topic;
        private boolean enabled;
        private String autoOffsetReset;
        private boolean enableAutoCommit;
        private Map<String, Object> properties;

    }

    @Data
    public static class Producer {
        private String topic;
        private Map<String, Object> properties;
    }
}