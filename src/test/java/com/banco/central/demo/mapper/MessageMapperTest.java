package com.banco.central.demo.mapper;

import com.banco.central.demo.dto.MessageRequestDTO;
import com.banco.central.demo.repository.model.KafkaModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.banco.central.demo.dto.MessageRequestDTO.Metadata;
import org.modelmapper.ModelMapper;

import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MessageMapperTest {

	private MessageMapper messageMapper;

	@BeforeEach
	void setUp() {
		messageMapper = new MessageMapper(new ModelMapper());
	}

	@Test
	void toEntity_success() {
		// Datos de prueba
		Metadata metadata = new Metadata("app1", "notification");
		MessageRequestDTO dto = MessageRequestDTO.builder()
				.id("3363")
				.message("Este es un mensaje de prueba")
				.timestamp(Instant.parse("2024-12-23T11:19:32Z"))
				.metadata(metadata)
				.build();

		String estado = "PENDIENTE";
		String uuid = UUID.randomUUID().toString();

		// Convertir a entidad
		KafkaModel kafkaModel = messageMapper.toEntity(dto, estado, uuid);

		// Verificaciones
		assertNotNull(kafkaModel);
		assertEquals(UUID.fromString(uuid), kafkaModel.getUuidKafka());
		assertEquals("PENDIENTE", kafkaModel.getEstado());
		assertEquals("app1", kafkaModel.getMetadata().get("source"));
		assertEquals("notification", kafkaModel.getMetadata().get("type"));
		assertEquals(dto.getMessage(), kafkaModel.getMessage());
	}

}
