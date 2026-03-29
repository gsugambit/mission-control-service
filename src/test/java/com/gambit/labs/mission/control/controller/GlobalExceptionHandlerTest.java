package com.gambit.labs.mission.control.controller;

import static com.gambit.labs.mission.control.exception.ExceptionResponse.OVERRIDE_EXCEPTION_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gambit.labs.mission.control.exception.DataViolationException;
import com.gambit.labs.mission.control.exception.ExceptionResponse;
import com.gambit.labs.mission.control.exception.InvalidRequestException;
import com.gambit.labs.mission.control.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler handler;
  private HttpServletRequest request;

  @BeforeEach
  void setUp() {
    handler = new GlobalExceptionHandler();
    request = mock(HttpServletRequest.class);
    when(request.getRequestURI()).thenReturn("/test-uri");
  }

  @Test
  void handleMethodArgumentNotValidException_returns_400() {
    // given
    MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
    BindingResult bindingResult = mock(BindingResult.class);
    FieldError fieldError = new FieldError("object", "field", "rejected", false, null, null,
        "message");
    when(ex.getBindingResult()).thenReturn(bindingResult);
    when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));

    // when
    ResponseEntity<?> response = handler.handleMethodArgumentNotValidException(request, ex);

    // then
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ExceptionResponse body = (ExceptionResponse) response.getBody();
    assertNotNull(body);
    assertEquals("field: field rule: message violated with value: rejected", body.getMessage());
  }

  @Test
  void handleMissingServletRequestPartException_returns_400() {
    // given
    MissingServletRequestPartException ex = new MissingServletRequestPartException("part");

    // when
    ResponseEntity<?> response = handler.handleMissingServletRequestPartException(request, ex);

    // then
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void handleMissingServletRequestParameterException_returns_400() {
    // given
    MissingServletRequestParameterException ex = new MissingServletRequestParameterException(
        "param", "type");

    // when
    ResponseEntity<?> response = handler.handleMissingServletRequestParameterException(request, ex);

    // then
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void handlePropertyReferenceException_returns_400() {
    // given
    PropertyReferenceException ex = mock(PropertyReferenceException.class);

    // when
    ResponseEntity<?> response = handler.handlePropertyReferenceException(request, ex);

    // then
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void handleMissingRequestHeaderException_returns_400() {
    // given
    MissingRequestHeaderException ex = mock(MissingRequestHeaderException.class);

    // when
    ResponseEntity<?> response = handler.handleMissingRequestHeaderException(request, ex);

    // then
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  void handleNoResourceFoundException_returns_400() {
    // given
    NoResourceFoundException ex = mock(NoResourceFoundException.class);

    // when
    ResponseEntity<?> response = handler.handleNoResourceFoundException(request, ex);

    // then
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ExceptionResponse body = (ExceptionResponse) response.getBody();
    assertNotNull(body);
    assertEquals(OVERRIDE_EXCEPTION_MESSAGE, body.getMessage());

  }

  @Test
  void handleHttpRequestMethodNotSupportedException_returns_405() {
    // given
    HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("POST");

    // when
    ResponseEntity<?> response = handler.handleHttpRequestMethodNotSupportedException(request, ex);

    // then
    assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
  }

  @Test
  void handleInvalidRequestException_returns_400() {
    // given
    InvalidRequestException ex = new InvalidRequestException("Invalid request");

    // when
    ResponseEntity<?> response = handler.handleInvalidRequestException(request, ex);

    // then
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ExceptionResponse body = (ExceptionResponse) response.getBody();
    assertNotNull(body);
    assertEquals("Invalid request", body.getMessage());
  }

  @Test
  void handleNotFound_returns_404() {
    // given
    NotFoundException ex = new NotFoundException("Not found");

    // when
    ResponseEntity<?> response = handler.handleNotFound(request, ex);

    // then
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    ExceptionResponse body = (ExceptionResponse) response.getBody();
    assertNotNull(body);
    assertEquals("Not found", body.getMessage());
  }

  @Test
  void handleDataViolation_returns_409() {
    // given
    DataViolationException ex = new DataViolationException("Data violation");

    // when
    ResponseEntity<?> response = handler.handleDataViolation(request, ex);

    // then
    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    ExceptionResponse body = (ExceptionResponse) response.getBody();
    assertNotNull(body);
    assertEquals("Data violation", body.getMessage());
  }

  @Test
  void handleDataIntegrityViolationException_returns_500() {
    // given
    DataIntegrityViolationException ex = new DataIntegrityViolationException("Database error");

    // when
    ResponseEntity<?> response = handler.handleDataIntegrityViolationException(request, ex);

    // then
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    ExceptionResponse body = (ExceptionResponse) response.getBody();
    assertNotNull(body);
    assertEquals(OVERRIDE_EXCEPTION_MESSAGE, body.getMessage());
  }

  @Test
  void handleGeneralException_returns_500() {
    // given
    Exception ex = new Exception("General error");

    // when
    ResponseEntity<?> response = handler.handleGeneralException(request, ex);

    // then
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    ExceptionResponse body = (ExceptionResponse) response.getBody();
    assertNotNull(body);
    assertEquals(OVERRIDE_EXCEPTION_MESSAGE, body.getMessage());
  }
}
