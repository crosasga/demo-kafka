package com.banco.central.demo.controller;

import com.banco.central.demo.constant.Constant;
import com.banco.central.demo.dto.MessageRequestDTO;
import com.banco.central.demo.dto.MessageResponseDTO;
import com.banco.central.demo.service.KafkaConsumerService;
import com.banco.central.demo.service.KafkaProducerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/queues")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Kafka Controller", description = "API para enviar y obtener mensajes de Kafka")
public class MessageController {


    private final KafkaProducerService producerService;
    private final KafkaConsumerService consumerService;


    @PostMapping
    @Operation(summary = "Enviar mensaje a Kafka con un ID único")
    public ResponseEntity<MessageResponseDTO> sendMessage(@Valid @RequestBody MessageRequestDTO messageRequestDTO) {
        if (messageRequestDTO.getTimestamp() == null) {
            messageRequestDTO.setTimestamp(Instant.now());  // Asigna la fecha y hora actual
        }
        String messageId = producerService.sendMessage(messageRequestDTO);
        log.info("sendMessage ID {}", messageId);
        log.info("sendMessage messageRequestDTO{}", messageRequestDTO.toString());
        return new ResponseEntity<>(MessageResponseDTO.builder().message(Constant.MENSAJE)
                .status(Constant.SUCCESS).id(messageId).build(), HttpStatus.OK);

    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener mensaje por ID")
    public ResponseEntity<?> getMessage(@PathVariable String id) {
        MessageRequestDTO message = consumerService.buscarMensajePorKey(id);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/all")
    @Operation(summary = "Obtener todos los mensajes de la cola que estan")
    public ResponseEntity<?> getAllMessage() {
        List<MessageRequestDTO> message = consumerService.listAllMessages();
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar mensaje por ID")
    public ResponseEntity<String> deleteMessage(@PathVariable String id) {
        boolean deleted = producerService.deleteMessageById(id);
        if (deleted) {
            log.info("deleteMessage ID {}", id);
            return ResponseEntity.ok("Mensaje eliminado exitosamente");
        } else {
            log.info("deleteMessage no existe");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Mensaje no encontrado");
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Verificar estado del API")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Kafka API está funcionando");
    }
}
