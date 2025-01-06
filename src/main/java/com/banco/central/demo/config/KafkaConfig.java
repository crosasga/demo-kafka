package com.banco.central.demo.config;

import com.banco.central.demo.dto.MessageRequestDTO;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


@Configuration
@Slf4j
@RequiredArgsConstructor
@Data
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String KAFKA_SERVER;
    private final KafkaPropertiesConfig kafkaProperties;


    // 1. Bean para crear el `ProducerFactory`
    @Bean
    public ProducerFactory<String, MessageRequestDTO> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);  // JSON
        configProps.put(ProducerConfig.RETRIES_CONFIG, 2);  // Número de reintentos
        configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 10);  // Tiempo entre reintentos (en ms)
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    // 2. Bean para crear `KafkaTemplate`
    @Bean
    public KafkaTemplate<String, MessageRequestDTO> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // 3. Bean para crear `ConsumerFactory`
    @Bean
    @Scope(scopeName = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public ConsumerFactory<String, MessageRequestDTO> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER);
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, "banco-busqueda");
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 2);  // Número de reintentos
        configProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 10);  // Tiempo entre reintentos (en ms)
        JsonDeserializer<MessageRequestDTO> deserializer = new JsonDeserializer<>(MessageRequestDTO.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(true);
        return new DefaultKafkaConsumerFactory<>(configProps, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MessageRequestDTO> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, MessageRequestDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    // 5. Bean para crear el `Topic`
   /* @Bean
    public NewTopic topic() {
        // Un `partition` y un `replica`
        return new NewTopic("banco-topic", 1, (short) 1)
                .configs(Map.of(
                "cleanup.policy", "compact",     // Política de compactación
                "delete.retention.ms", "100",  // Retención de tombstones en 1 minuto
                "segment.ms", "100"            //
        ));
    }*/

    @Bean
    public NewTopic topic() {
        // Un `partition` y un `replica`
        return new NewTopic("banco-topic", 1, (short) 1);
    }

    @Bean(name = "kafkaConsumerConfigProperties")
    public Properties kafkaConsumerProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "banco-busqueda");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);  // Evitar que avance automáticamente
        props.put(ProducerConfig.RETRIES_CONFIG, 2);  // Número de reintentos
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 10);  // Tiempo entre reintentos (en ms)
        //props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        JsonDeserializer<MessageRequestDTO> deserializer = new JsonDeserializer<>(MessageRequestDTO.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");  // Confiar en todos los paquetes
        deserializer.setUseTypeMapperForKey(false);  // No mapear Key
       // props.put("value.deserializer", deserializer.getClass().getName());
        return props;
    }

    @Bean(name = "ConsumerFactorygProperties")
    public ConsumerFactory<String, MessageRequestDTO> consumerFactoryProperties() {
        JsonDeserializer<MessageRequestDTO> deserializer = new JsonDeserializer<>(MessageRequestDTO.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");  // Confiar en todos los paquetes
        deserializer.setUseTypeMapperForKey(false);  // No mapear Key

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        props.put(ConsumerConfig.GROUP_ID_CONFIG, "banco-busqueda");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
       // props.put("spring.json.fail-on-empty-payload", false);  // Evita error cuando el payload está vacío

        props.put(ProducerConfig.RETRIES_CONFIG, 2);  // Número de reintentos
        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 10);  // Tiempo entre reintentos (en ms)

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }
}