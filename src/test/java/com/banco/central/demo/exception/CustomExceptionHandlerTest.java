package com.banco.central.demo.exception;

import com.banco.central.demo.dto.MessageErrorDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomExceptionHandlerTest {

	private final CustomExceptionHandler exceptionHandler = new CustomExceptionHandler();

	@Test
	void handleErrorExceptionMessage_returnsBadRequest() {
		// Crea un objeto de error
		MessageErrorDTO errorDTO = new MessageErrorDTO("ERR001", "Detalle del error de validación","31");
		ErrorExceptionMessage exception = new ErrorExceptionMessage("Error de prueba", errorDTO);

		ResponseEntity<Map<String, String>> response = exceptionHandler.handleErrorExceptionMessage(exception);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("Error de validación", response.getBody().get("error"));
		assertEquals("Error de prueba", response.getBody().get("message"));
	}

	@Test
	void handleGeneralException_returnsInternalServerError() {
		Exception exception = new Exception("Error inesperado");

		ResponseEntity<String> response = exceptionHandler.handleGeneralException(exception);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertEquals("Error inesperado: Error inesperado", response.getBody());
	}

	@Test
	void handleMethodArgumentNotValidException_returnsBadRequest() {
		// Mock del `BindingResult` con errores de campo
		BindingResult bindingResult = mock(BindingResult.class);
		FieldError fieldError = new FieldError("objectName", "id", "El Id no puede estar vacío");
		when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

		MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

		ResponseEntity<Map<String, String>> response = exceptionHandler.handleErrorExceptionMessage(exception);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("El Id no puede estar vacío", response.getBody().get("id"));
	}

	@Test
	void handleHttpMessageNotReadableException_returnsBadRequest() {
		HttpMessageNotReadableException exception = new HttpMessageNotReadableException("No se pudo leer el mensaje", null, null);

		ResponseEntity<Map<String, String>> response = exceptionHandler.handleHttpMessageNotReadableException(exception);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("Error de validación", response.getBody().get("error"));
		assertTrue(response.getBody().get("message").contains("mensaje"));
	}
}