package com.banco.central.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.NoArgsConstructor;


@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponseDTO
{
        private String status;
        private String message;
        private String id;
}
