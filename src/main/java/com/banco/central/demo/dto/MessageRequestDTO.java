package com.banco.central.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)

public class MessageRequestDTO {


    private String  uuidKafka;
    @NotNull(message = "El Id no puede estar nulo")
    @NotEmpty(message = "El Id no puede estar vacio")
    private String id;        // Identificador único del mensaje
    private String message;   // Contenido del mensaje
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")  // Formato de fecha esperado
    private Instant timestamp; // Fecha y hora en formato Instant
    private Metadata metadata; // Objeto anidado Metadata

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Metadata {
        private String source;
        private String type;
    }
}
