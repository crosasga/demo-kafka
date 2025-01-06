package com.banco.central.demo.service;

import com.banco.central.demo.constant.Constant;
import com.banco.central.demo.dto.MessageErrorDTO;
import com.banco.central.demo.dto.MessageRequestDTO;
import com.banco.central.demo.exception.ErrorExceptionMessage;
import com.banco.central.demo.mapper.MessageMapper;
import com.banco.central.demo.repository.KafkaPostgreRepository;
import com.banco.central.demo.repository.model.KafkaModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private final KafkaTemplate<String, MessageRequestDTO> kafkaTemplate;
    private final KafkaPostgreRepository mensajeRepository;
    @Value("${spring.kafka.producer.topic}")
    private String topicName;
    @Autowired
    private MessageMapper messageMapper;

    // Almacenamiento temporal para los mensajes
    private final Map<String, MessageRequestDTO> messageStore = new ConcurrentHashMap<>();

    public String sendMessage(MessageRequestDTO message) {
        String uniqueId = UUID.randomUUID().toString();  // Genera un ID único
        message.setUuidKafka(uniqueId);
        try {
            messageStore.put(uniqueId, message);  // Guarda el mensaje en memoria
            kafkaTemplate.send(topicName, uniqueId, message);
            log.info("Mensaje enviado con ID: {}", uniqueId);
            return uniqueId;
        } catch (TimeoutException e) {
            // Error específico cuando Kafka no responde dentro del tiempo esperado
            log.error("Kafka no está disponible (Timeout): {}", e.getMessage());
            KafkaModel kafkaModel = messageMapper.toEntity(message, Constant.ESTADO_PENDIENTE, uniqueId);
            kafkaModel.setError("Timeout al conectar con Kafka");
            mensajeRepository.save(kafkaModel);
            throw new ErrorExceptionMessage(MessageErrorDTO.builder().message(Constant.MENSAJE_NO_ENVIADO)
                    .status(Constant.ESTADO_ERROR).id(uniqueId).build(), e.getMessage().toString());
        } catch (KafkaException e) {
            // Otras excepciones de Kafka
            log.error("Error de Kafka: {}", e.getMessage());
            KafkaModel kafkaModel = messageMapper.toEntity(message, Constant.ESTADO_PENDIENTE, uniqueId);
            kafkaModel.setError(e.getMessage());
            mensajeRepository.save(kafkaModel);
            // throw new RuntimeException("Error al enviar mensaje a Kafka", e);
            throw new ErrorExceptionMessage(MessageErrorDTO.builder().message(Constant.MENSAJE_NO_ENVIADO)
                    .status(Constant.ESTADO_ERROR).id(uniqueId).build(), e.getMessage().toString());


        } catch (Exception e) {
            // Otras excepciones no específicas de Kafka
            log.error("Error inesperado al enviar mensaje: {}", e.getMessage());
            KafkaModel kafkaModel = messageMapper.toEntity(message, Constant.ESTADO_ERROR, uniqueId);
            kafkaModel.setError("Error desconocido: " + e.getMessage());
            mensajeRepository.save(kafkaModel);
            throw new ErrorExceptionMessage(MessageErrorDTO.builder().message(Constant.MENSAJE_NO_ENVIADO)
                    .status(Constant.ESTADO_ERROR).id(uniqueId).build(), e.getMessage().toString());


        }
    }

    public boolean deleteMessageById(String id) {
        try {
            kafkaTemplate.send(topicName, id, MessageRequestDTO.builder().uuidKafka(id).build()).get();  // Enviar "tombstone"
            return true;
        } catch (Exception e) {
            throw new ErrorExceptionMessage(MessageErrorDTO.builder().message(Constant.MENSAJE_NO_ENVIADO)
                    .status(Constant.ESTADO_ERROR).id(id).build(), e.getMessage().toString());
        }

    }


}