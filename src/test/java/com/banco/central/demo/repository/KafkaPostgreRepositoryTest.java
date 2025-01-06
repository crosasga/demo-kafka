package com.banco.central.demo.repository;

import com.banco.central.demo.repository.model.KafkaModel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class KafkaPostgreRepositoryTest {
	@Autowired
	private KafkaPostgreRepository kafkaPostgreRepository;

	@Disabled("Prueba deshabilitada temporalmente")
	@Test
	void saveAndFindById() {
		KafkaModel kafkaModel = KafkaModel.builder()
				.id("3363")
				.uuidKafka(UUID.randomUUID())
				.message("Este es un mensaje de prueba")
				.timestamp(LocalDateTime.now())
				.metadata(Map.of("source", "app1", "type", "notification"))
				.estado("PENDIENTE")
				.build();

		kafkaPostgreRepository.save(kafkaModel);

		Optional<KafkaModel> found = kafkaPostgreRepository.findById("3363");
		assertTrue(found.isPresent());
		assertEquals("Este es un mensaje de prueba", found.get().getMessage());
	}

}