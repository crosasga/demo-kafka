package com.banco.central.demo.repository.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "kafka_message")
public class KafkaModel {
    @Id
    @Column(name = "id", nullable = false, unique = false)
    private String id;  // Mapea la columna `id` (VARCHAR)

    @Column(name = "uuid_kafka", nullable = false, unique = true)
    private UUID uuidKafka;  // UUID generado para Kafka

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;  // Mapea la columna `message` (TEXT)

    @Column(name = "timestamp")
    private LocalDateTime timestamp;  // Mapea la columna `timestamp` (TIMESTAMP)

    @Column(name = "metadata", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metadata;
   // private String metadata;  // Mapea la columna `metadata` (JSONB) como `String`

    @Column(name = "estado", nullable = false, length = 50)
    @JsonProperty(defaultValue = "PENDIENTE")
    private String estado;  // Estado del mensaje (por defecto "PENDIENTE")

    @Column(name = "error", columnDefinition = "TEXT")
    private String error;  // Detalle de error, si ocurre

}
