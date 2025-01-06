package com.banco.central.demo.mapper;

import com.banco.central.demo.dto.MessageRequestDTO;
import com.banco.central.demo.repository.model.KafkaModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MessageMapper {

    private final ModelMapper modelMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public KafkaModel toEntity(MessageRequestDTO dto, String estado, String uuid) {
        KafkaModel kafkaModel = modelMapper.map(dto, KafkaModel.class);
        // Configuración manual para campos que no se mapearon automáticamente
        kafkaModel.setUuidKafka(UUID.fromString(uuid)); // Genera un nuevo UUID
        kafkaModel.setEstado(estado); // Asigna un estado predeterminado
        // Convertir metadata de String a JsonNode antes de guardar
        if (dto.getMetadata() != null) {
            String metadataString = null;
            try {
                metadataString = objectMapper.writeValueAsString(dto.getMetadata());
                JsonNode jsonNode = objectMapper.readTree(metadataString);
                kafkaModel.setMetadata(objectMapper.convertValue(jsonNode, Map.class));  // Convertir a Map
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

        }
        kafkaModel.setTimestamp(dto.getTimestamp() != null ? dto.getTimestamp().atZone(java.time.ZoneOffset.UTC).toLocalDateTime() : null);
        return kafkaModel;
    }
}

