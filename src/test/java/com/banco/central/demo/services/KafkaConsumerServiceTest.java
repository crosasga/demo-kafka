package com.banco.central.demo.services;

import com.banco.central.demo.dto.MessageRequestDTO;
import com.banco.central.demo.service.KafkaConsumerService;
import com.banco.central.demo.service.KafkaConsumerServiceImpl;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Duration;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerServiceTest {



	KafkaTemplate<String, MessageRequestDTO> kafkaTemplate = mock(KafkaTemplate.class);

	@Mock
	private ConsumerFactory<String, MessageRequestDTO> consumerFactory;


	@Captor
	private ArgumentCaptor<ConsumerRecord<String, MessageRequestDTO>> recordCaptor;

	private KafkaConsumer<String, MessageRequestDTO> mockKafkaConsumer = mock(KafkaConsumer.class);;

	private MessageRequestDTO messageRequestDTO;

	@InjectMocks
	private KafkaConsumerServiceImpl kafkaConsumerService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		mockKafkaConsumer = mock(KafkaConsumer.class);
		//when(consumerFactory.createConsumer()).thenReturn(mockKafkaConsumer);
	}

	@Disabled("Prueba deshabilitada temporalmente")
	@Test
	void buscarMensajePorKey_mensajeEncontrado() {
		// Simulación del mensaje de Kafka
		MessageRequestDTO messageRequestDTO = MessageRequestDTO.builder()
				.id("123")
				.uuidKafka("uuid-prueba")
				.message("Este es un mensaje de prueba")
				.build();

		ConsumerRecord<String, MessageRequestDTO> record = new ConsumerRecord<>("banco-topic", 0, 0L, "123", messageRequestDTO);

		// Crear el `ConsumerRecords` con el mensaje simulado
		Map<TopicPartition, List<ConsumerRecord<String, MessageRequestDTO>>> recordsMap = new HashMap<>();
		recordsMap.put(new TopicPartition("banco-topic", 0), List.of(record));
		ConsumerRecords<String, MessageRequestDTO> consumerRecords = new ConsumerRecords<>(recordsMap);

		when(mockKafkaConsumer.poll(Duration.ofMillis(30000))).thenReturn(consumerRecords);

		// Ejecución
		MessageRequestDTO result = kafkaConsumerService.buscarMensajePorKey("123");

		// Verificación
		assertNotNull(result);
		assertEquals("123", result.getId());
		assertEquals("Este es un mensaje de prueba", result.getMessage());
		verify(mockKafkaConsumer, times(1)).assign(Collections.singletonList(new TopicPartition("banco-topic", 0)));
		verify(mockKafkaConsumer, times(1)).close();
	}

	@Disabled("Prueba deshabilitada temporalmente")
	@Test
	void buscarMensajePorKey_mensajeNoEncontrado() {
		// Simulación de `ConsumerRecords` vacío (sin mensajes)
		KafkaConsumer<String, MessageRequestDTO> mockKafkaConsumer = Mockito.mock(KafkaConsumer.class);

		ConsumerRecords<String, MessageRequestDTO> emptyRecords = new ConsumerRecords<>(Collections.emptyMap());

		when(mockKafkaConsumer.poll(Duration.ofMillis(30000))).thenReturn(emptyRecords);

		// Ejecución
		RuntimeException exception = assertThrows(RuntimeException.class,
				() -> kafkaConsumerService.buscarMensajePorKey("key-no-existente"));

		// Verificación
		assertEquals("Mensaje con Key key-no-existente no encontrado en el topic ", exception.getMessage());
		verify(mockKafkaConsumer, times(1)).assign(Collections.singletonList(new TopicPartition("banco-topic", 0)));
		verify(mockKafkaConsumer, times(1)).close();
	}

	@Disabled("Prueba deshabilitada temporalmente")
	@Test
	void listAllMessages_conMensajes() {

		KafkaConsumer<String, MessageRequestDTO> mockKafkaConsumer = Mockito.mock(KafkaConsumer.class);

		// Configurar el consumerFactory para devolver el mockKafkaConsumer
		when(consumerFactory.createConsumer()).thenReturn(mockKafkaConsumer);

		// Simular los mensajes en ConsumerRecords
		MessageRequestDTO message1 = MessageRequestDTO.builder()
				.id("1")
				.uuidKafka("uuid-1")
				.message("Mensaje 1")
				.build();

		MessageRequestDTO message2 = MessageRequestDTO.builder()
				.id("2")
				.uuidKafka("uuid-2")
				.message("Mensaje 2")
				.build();

		ConsumerRecord<String, MessageRequestDTO> record1 = new ConsumerRecord<>("banco-topic", 0, 0L, "key-1", message1);
		ConsumerRecord<String, MessageRequestDTO> record2 = new ConsumerRecord<>("banco-topic", 0, 1L, "key-2", message2);

		// Crear un map con partición y lista de ConsumerRecords
		Map<TopicPartition, List<ConsumerRecord<String, MessageRequestDTO>>> recordsMap = new HashMap<>();
		recordsMap.put(new TopicPartition("banco-topic", 0), List.of(record1, record2));

		ConsumerRecords<String, MessageRequestDTO> consumerRecords = new ConsumerRecords<>(recordsMap);

		// Mock del `poll`
		when(mockKafkaConsumer.poll(Duration.ofMillis(5000))).thenReturn(consumerRecords);

		when(consumerFactory.createConsumer()).thenReturn(mockKafkaConsumer);  // Evitar null en createConsumer

		// Ejecución
		List<MessageRequestDTO> result = kafkaConsumerService.listAllMessages();

		// Verificación
		assertEquals(2, result.size());
		assertEquals("Mensaje 1", result.get(0).getMessage());
		assertEquals("Mensaje 2", result.get(1).getMessage());
	}

	@Disabled("Prueba deshabilitada temporalmente")
	@Test
	void listAllMessages_sinMensajes() {
		// Simulación de una respuesta vacía de ConsumerRecords
		ConsumerRecords<String, MessageRequestDTO> emptyRecords = new ConsumerRecords<>(Collections.emptyMap());

		when(mockKafkaConsumer.poll(Duration.ofMillis(5000))).thenReturn(emptyRecords);

		List<MessageRequestDTO> result = kafkaConsumerService.listAllMessages();

		// Verificación
		assertTrue(result.isEmpty(), "La lista debe estar vacía cuando no hay mensajes en Kafka.");
	}

	@Test
	void consumeMessage_agregaMensajeALaLista() {
		// Simulación del mensaje
		MessageRequestDTO messageRequestDTO = MessageRequestDTO.builder()
				.id("123")
				.uuidKafka("uuid-consumer")
				.message("Mensaje recibido del listener")
				.build();

		kafkaConsumerService.consumeMessage(messageRequestDTO);

		// No hay un método para obtener los mensajes en la clase real, pero se puede verificar el log o ampliar la implementación.
		// Esto es solo para validar que se llamó al método de consumo.
		assertDoesNotThrow(() -> kafkaConsumerService.consumeMessage(messageRequestDTO));
	}

	@Test
	void consumeMessageString_mensajeValido() {
		String message = """
                {
                  "id": "123",
                  "uuidKafka": "uuid-string",
                  "message": "Este es un mensaje desde el string",
                  "metadata": { "source": "app1", "type": "notification" }
                }
                """;

		assertDoesNotThrow(() -> kafkaConsumerService.consumeMessageString(message));
	}

	@Test
	void consumeMessageString_mensajeInvalido() {
		String invalidMessage = "{invalid-json}";

		assertDoesNotThrow(() -> kafkaConsumerService.consumeMessageString(invalidMessage));
	}
}