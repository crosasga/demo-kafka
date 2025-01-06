package com.banco.central.demo.service;

import com.banco.central.demo.config.KafkaPropertiesConfig;
import com.banco.central.demo.dto.MessageRequestDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class KafkaConsumerServiceImpl implements KafkaConsumerService {

    //private final Properties consumerProperties;
    private final ConsumerFactory<String, MessageRequestDTO> consumerFactory;
    private final List<MessageRequestDTO> mensajes = new ArrayList<>();
    @Value("${spring.kafka.consumer.topic}")
    private String topicName;
    @Value("${spring.kafka.consumer.group-id}")
    private String grupId;

    public KafkaConsumerServiceImpl(@Qualifier("ConsumerFactorygProperties") ConsumerFactory<String, MessageRequestDTO> consumerFactory, KafkaPropertiesConfig kafkaProperties) {
        this.consumerFactory = consumerFactory;
    }

    public MessageRequestDTO buscarMensajePorKey(@Header(KafkaHeaders.RECEIVED_KEY) String key) {
        // Crear el KafkaConsumer utilizando el ConsumerFactory
        try (KafkaConsumer<String, MessageRequestDTO> consumer = (KafkaConsumer<String, MessageRequestDTO>) consumerFactory.createConsumer()) {

            // Asignar la partición 0
            TopicPartition partition0 = new TopicPartition(topicName, 0);
            consumer.assign(Collections.singletonList(partition0));
            consumer.seek(partition0, 0);  // Iniciar lectura desde el offset 0

            // Leer mensajes durante 30 segundos
            int intentos = 0;
            while (intentos < 2) {
                for (ConsumerRecord<String, MessageRequestDTO> record : consumer.poll(Duration.ofMillis(30000))) {
                    // log.info("Key: {}, Offset: {}, Mensaje: {}", record.key(), record.offset(), record.value().getMessage());
                    if (record.value() == null) {
                        log.info("El mensaje con key {} es un tombstone (eliminado).", key);
                        throw new RuntimeException("El mensaje con key " + key + " fue eliminado.");
                    }
                    log.info("Mensaje encontrado con key {}: {}", key, record.value());
                    return record.value();  // Retorna el mensaje si coincide la Key
                }
                intentos++;
            }
        }
        throw new RuntimeException("Mensaje con Key " + key + " no encontrado en el topic ");
    }


    public List<MessageRequestDTO> listAllMessages() {
        List<MessageRequestDTO> mensajesAll = new ArrayList<>();
        // Crear el KafkaConsumer utilizando el ConsumerFactory
        try (KafkaConsumer<String, MessageRequestDTO> consumer = (KafkaConsumer<String, MessageRequestDTO>) consumerFactory.createConsumer()) {

            // Asignar la partición 0
            TopicPartition partition0 = new TopicPartition(topicName, 0);
            consumer.assign(Collections.singletonList(partition0));
            consumer.seek(partition0, 0);  // Iniciar lectura desde el offset 0

            boolean moreRecords = true;
            while (moreRecords) {
                var records = consumer.poll(Duration.ofMillis(5000));  // Tiempo de espera por mensajes
                if (records.isEmpty()) {
                    moreRecords = false;  // Detener la lectura si no hay más mensajes
                } else {
                    for (ConsumerRecord<String, MessageRequestDTO> record : records) {
                        //log.info("Key: {}, Offset: {}, Mensaje: {}", record.key(), record.offset(), record.value().getMessage());
                        if (record.value() != null) {
                            mensajesAll.add(record.value());
                        } else {
                            mensajesAll.removeIf(mensaje ->
                                    mensaje.getUuidKafka() != null && mensaje.getUuidKafka().equals(record.key()));
                            log.info("Se eliminó el mensaje con key: {}", record.key());
                        }

                    }
                }

                // Crear un mapa de conteo de uuidKafka
                Map<String, Long> conteoUuid = mensajesAll.stream()
                        .filter(mensaje -> mensaje.getUuidKafka() != null)  // Ignorar mensajes con uuidKafka nulo
                        .collect(Collectors.groupingBy(MessageRequestDTO::getUuidKafka, Collectors.counting()));

                // Filtrar los mensajes que tienen un uuidKafka único (no duplicados)
                List<MessageRequestDTO> mensajesSinDuplicados = mensajesAll.stream()
                        .filter(mensaje -> mensaje.getUuidKafka() == null || conteoUuid.get(mensaje.getUuidKafka()) == 1)
                        .collect(Collectors.toList());
                return mensajesSinDuplicados;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return mensajesAll;
    }


    @KafkaListener(topics = "banco-topic", groupId = "banco-busqueda}", containerFactory = "kafkaListenerContainerFactory")
    public void consumeMessage(@Payload(required = false) MessageRequestDTO message) {
        log.info("Mensaje recibido: {}", message);
        try {
            log.info("resultado de consumer {} ", message.toString());
            mensajes.add(message);
        } catch (Exception e) {
            log.error("error en el consumer {} ", e.getMessage());
        }

    }


    //si necesitamos hacer parseo de string a objeto
    public void consumeMessageString(String message) {
        log.info("Mensaje recibido: {}", message);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
            MessageRequestDTO messageRequestDTO = objectMapper.readValue(message, MessageRequestDTO.class);
            log.info("resultado de consumer {} ", messageRequestDTO.toString());
        } catch (JsonProcessingException ex) {
            log.error("error en el consumer al convertir {} ", ex.getMessage());
        } catch (Exception e) {
            log.error("error en el consumer {} ", e.getMessage());
        }
    }

}