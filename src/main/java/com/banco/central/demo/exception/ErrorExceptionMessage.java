package com.banco.central.demo.exception;

import com.banco.central.demo.dto.MessageErrorDTO;

public class ErrorExceptionMessage extends RuntimeException {

    private final MessageErrorDTO errorDTO;

    public ErrorExceptionMessage(String message, MessageErrorDTO errorDTO) {
        super(message);
        this.errorDTO = errorDTO;
    }

    public ErrorExceptionMessage(MessageErrorDTO errorDTO, String message) {
        super(message);  // Mensaje de excepción para logs
        this.errorDTO = errorDTO;
    }
}
