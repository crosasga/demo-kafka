package com.banco.central.demo.services;

import com.banco.central.demo.constant.Constant;
import com.banco.central.demo.dto.MessageRequestDTO;
import com.banco.central.demo.exception.ErrorExceptionMessage;
import com.banco.central.demo.mapper.MessageMapper;
import com.banco.central.demo.repository.KafkaPostgreRepository;
import com.banco.central.demo.repository.model.KafkaModel;
import com.banco.central.demo.service.KafkaProducerService;
import com.banco.central.demo.service.KafkaProducerServiceImpl;
import org.apache.kafka.common.errors.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceTest {



	KafkaTemplate<String, MessageRequestDTO> kafkaTemplate = mock(KafkaTemplate.class);


	@Mock
	private KafkaPostgreRepository mensajeRepository = mock(KafkaPostgreRepository.class);;

	@Mock
	private MessageMapper messageMapper;

	@Captor
	private ArgumentCaptor<KafkaModel> kafkaModelCaptor;

	private MessageRequestDTO messageRequestDTO;

	@InjectMocks
	private KafkaProducerService kafkaProducerService = new KafkaProducerServiceImpl(
			kafkaTemplate, mensajeRepository);
	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		messageRequestDTO = MessageRequestDTO.builder()
				.id("123")
				.message("Este es un mensaje de prueba")
				.metadata(new MessageRequestDTO.Metadata("app1", "notification"))
				.build();
		ReflectionTestUtils.setField(kafkaProducerService, "topicName", "banco-topic");  // Inyección manual

	}


	@Test
	void sendMessage_success() {
		MessageRequestDTO messageRequestDTO = MessageRequestDTO.builder()
				.id("123")
				.message("Prueba de Kafka")
				.build();
		KafkaModel kafkaModel = KafkaModel.builder()
				.id("123")
				.uuidKafka(UUID.randomUUID())
				.message("Prueba de Kafka")
				.estado("PENDIENTE")
				.build();

		when(kafkaTemplate.send(anyString(), anyString(), any(MessageRequestDTO.class)))
				.thenReturn(CompletableFuture.completedFuture(null));


		String id = kafkaProducerService.sendMessage(messageRequestDTO);
		assertNotNull(id);
		assertFalse(id.isEmpty());
	}

	@Test
	void sendMessage_timeoutException() {
		MessageRequestDTO messageRequestDTO = MessageRequestDTO.builder()
				.id("123")
				.message("Prueba de Kafka")
				.build();
		KafkaModel kafkaModel = KafkaModel.builder()
				.id("123")
				.uuidKafka(UUID.randomUUID())
				.message("Prueba de Kafka")
				.estado("PENDIENTE")
				.build();


		// Configurar los mocks
		when(messageMapper.toEntity(any(MessageRequestDTO.class), anyString(), anyString())).thenReturn(kafkaModel);

		when(kafkaTemplate.send(anyString(), anyString(), any(MessageRequestDTO.class)))
				.thenThrow(new TimeoutException("Kafka no disponible"));

		ErrorExceptionMessage exception = assertThrows(ErrorExceptionMessage.class,
				() -> kafkaProducerService.sendMessage(messageRequestDTO));

		assertEquals("Kafka no disponible", exception.getMessage());

	}

	@Test
	void sendMessage_kafkaException() {
		MessageRequestDTO messageRequestDTO = MessageRequestDTO.builder()
				.id("123")
				.message("Prueba de Kafka")
				.build();
		KafkaModel kafkaModel = KafkaModel.builder()
				.id("123")
				.uuidKafka(UUID.randomUUID())
				.message("Prueba de Kafka")
				.estado("PENDIENTE")
				.build();

		when(messageMapper.toEntity(any(MessageRequestDTO.class), anyString(), anyString())).thenReturn(kafkaModel);

		when(kafkaTemplate.send(anyString(), anyString(), any(MessageRequestDTO.class)))
				.thenThrow(new KafkaException("Error de Kafka"));

		assertThrows(ErrorExceptionMessage.class, () -> kafkaProducerService.sendMessage(messageRequestDTO));
		ErrorExceptionMessage exception = assertThrows(ErrorExceptionMessage.class,
				() -> kafkaProducerService.sendMessage(messageRequestDTO));

		// Verificar que el mensaje de la excepción coincide
		assertEquals("Error de Kafka", exception.getMessage());
	}

	@Test
	void sendMessage_generalException() {
		MessageRequestDTO messageRequestDTO = MessageRequestDTO.builder()
				.id("123")
				.message("Prueba de Kafka")
				.build();
		KafkaModel kafkaModel = KafkaModel.builder()
				.id("123")
				.uuidKafka(UUID.randomUUID())
				.message("Prueba de Kafka")
				.estado("PENDIENTE")
				.build();

		when(messageMapper.toEntity(any(MessageRequestDTO.class), anyString(), anyString())).thenReturn(kafkaModel);

		when(kafkaTemplate.send(anyString(), anyString(), any(MessageRequestDTO.class)))
				.thenThrow(new RuntimeException("Error inesperado"));
		//when(mensajeRepository.save(any(KafkaModel.class))).thenAnswer(invocation -> invocation.getArgument(0));

		assertThrows(Exception.class, () -> kafkaProducerService.sendMessage(messageRequestDTO));


		// Ejecutar el método y capturar la excepción lanzada
		ErrorExceptionMessage exception = assertThrows(ErrorExceptionMessage.class,
				() -> kafkaProducerService.sendMessage(messageRequestDTO));

		// Verificar que el mensaje de la excepción coincide
		assertEquals(Constant.ERROR_INESPERADO, exception.getMessage());

	}


	@Test
	void deleteMessageById_success() throws Exception {
		when(kafkaTemplate.send(anyString(), anyString(), any(MessageRequestDTO.class)))
				.thenReturn(CompletableFuture.completedFuture(null));

		boolean result = kafkaProducerService.deleteMessageById("123");

		assertTrue(result);
		verify(kafkaTemplate, times(1)).send(anyString(), eq("123"), any(MessageRequestDTO.class));
	}

	@Test
	void deleteMessageById_failure() {
		when(kafkaTemplate.send(anyString(), anyString(), any(MessageRequestDTO.class)))
				.thenThrow(new RuntimeException("Error al eliminar el mensaje"));

		assertThrows(ErrorExceptionMessage.class, () -> kafkaProducerService.deleteMessageById("123"));
	}

}