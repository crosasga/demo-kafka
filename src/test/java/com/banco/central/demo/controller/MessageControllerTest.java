package com.banco.central.demo.controller;

import com.banco.central.demo.dto.MessageRequestDTO;
import com.banco.central.demo.dto.MessageRequestDTO.Metadata;
import com.banco.central.demo.service.KafkaConsumerService;
import com.banco.central.demo.service.KafkaProducerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MessageControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Mock
	private KafkaProducerService kafkaProducerService;

	@Mock
	private KafkaConsumerService kafkaConsumerService;

	@Mock
	private KafkaConsumerService kafkaConsumerServiceAll;

	@InjectMocks
	private MessageController messageController;

	@BeforeEach
	void setup() {
		//MockitoAnnotations.openMocks(this);
		mockMvc = MockMvcBuilders.standaloneSetup(messageController).build();
	}

	@Test
	void sendMessage_success() throws Exception {
		// Configurar el ObjectMapper con el módulo JavaTimeModule
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.findAndRegisterModules(); // Registro adicional de otros módulos
		MessageRequestDTO request = MessageRequestDTO.builder()
				.id("12345")
				.message("Este es un mensaje de prueba")
				.timestamp(Instant.parse("2024-12-23T11:19:32Z"))
				.metadata(new Metadata("app1", "notification"))
				.build();
		mockMvc.perform(post("/api/v1/queues")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status", is("success")))
				.andExpect(jsonPath("$.message", is("Mensaje enviado con ID")));
	}

	@Test
	void deleteMessageByKey_success() throws Exception {
		String key = "3363";
		mockMvc.perform(delete("/api/v1/queues/{key}", key))
				.andExpect(status().isNoContent());  // 204 No Content

	}

	@Test
	void getMessageByKey_success() throws Exception {

		MessageRequestDTO response = MessageRequestDTO.builder()
				.id("32")
				.uuidKafka("2040e970-bd30-4a11-a7cc-9b7fb3b08d3e")
				.message("Este es un mensaje de prueba")
				.timestamp(Instant.parse("2024-12-23T11:19:32Z"))
				.metadata(new Metadata("app1", "notification"))
				.build();


		MvcResult result = mockMvc.perform(get("/api/v1/queues/{key}", "32")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
	}



	@Test
	void getAllMessages_emptyList() throws Exception {

		mockMvc.perform(get("/api/v1/queues/all")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())  // HTTP 200
				.andExpect(jsonPath("$.length()").value(0));  // La lista debe estar vacía
	}


	/*@Test
	void getMessageByKey_success_error() throws Exception {

		when(kafkaConsumerService.buscarMensajePorKey("3363")).thenReturn(MessageRequestDTO.builder().uuidKafka("3363").message("Este es un mensaje de prueba")
				.timestamp(Instant.parse("2024-12-23T11:19:32Z"))
				.metadata(new Metadata("app1", "notification")).build());

		mockMvc.perform(get("/api/v1/queues/{key}", "3363")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().is4xxClientError())
				.andExpect(jsonPath("$.id").value("3363"))
				.andExpect(jsonPath("$.message").value("Este es un mensaje de prueba"));
	}*/
}
