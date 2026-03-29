package com.gambit.labs.mission.control.controller;

import com.gambit.labs.mission.control.exception.DataViolationException;
import com.gambit.labs.mission.control.exception.ExceptionResponse;
import com.gambit.labs.mission.control.exception.InvalidRequestException;
import com.gambit.labs.mission.control.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleMethodArgumentNotValidException(HttpServletRequest req,
      MethodArgumentNotValidException ex) {
    LOGGER.info("MethodArgumentNotValidException", ex);
    ExceptionResponse exceptionResponse = ExceptionResponse.from(ex, req.getRequestURI());
    final String methodExceptionString = ex.getBindingResult().getAllErrors().stream()
        .map((error) -> "field: " + ((FieldError) error).getField() + " rule: "
            + error.getDefaultMessage()
            + " violated with value: " + ((FieldError) error).getRejectedValue())
        .collect(Collectors.joining(", "));
    exceptionResponse.setMessage(methodExceptionString);

    return ResponseEntity.status(HttpStatusCode.valueOf(400)).body(exceptionResponse);
  }

  @ExceptionHandler(MissingServletRequestPartException.class)
  public ResponseEntity<?> handleMissingServletRequestPartException(HttpServletRequest req,
      MissingServletRequestPartException ex) {
    LOGGER.info("MissingServletRequestPartException", ex);
    ExceptionResponse exceptionResponse = ExceptionResponse.from(ex, req.getRequestURI());
    return ResponseEntity.badRequest().body(exceptionResponse);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<?> handleMissingServletRequestParameterException(HttpServletRequest req,
      MissingServletRequestParameterException ex) {
    LOGGER.info("MissingServletRequestParameterException", ex);
    ExceptionResponse exceptionResponse = ExceptionResponse.from(ex, req.getRequestURI());
    return ResponseEntity.badRequest().body(exceptionResponse);
  }

  @ExceptionHandler(PropertyReferenceException.class)
  public ResponseEntity<?> handlePropertyReferenceException(HttpServletRequest req,
      PropertyReferenceException ex) {
    LOGGER.info("PropertyReferenceException", ex);
    ExceptionResponse exceptionResponse = ExceptionResponse.from(ex, req.getRequestURI());
    return ResponseEntity.badRequest().body(exceptionResponse);
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<?> handleMissingRequestHeaderException(HttpServletRequest req,
      MissingRequestHeaderException ex) {
    LOGGER.info("MissingRequestHeaderException", ex);
    ExceptionResponse exceptionResponse = ExceptionResponse.from(ex, req.getRequestURI());
    return ResponseEntity.badRequest().body(exceptionResponse);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<?> handleNoResourceFoundException(HttpServletRequest req,
      NoResourceFoundException ex) {
    LOGGER.info("Unexpected NoResourceFoundException", ex);
    ExceptionResponse exceptionResponse = ExceptionResponse.from(ex, req.getRequestURI());
    exceptionResponse.setMessage("");

    return ResponseEntity.badRequest().body(exceptionResponse);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<?> handleHttpRequestMethodNotSupportedException(HttpServletRequest req,
      HttpRequestMethodNotSupportedException ex) {
    LOGGER.info("Unexpected HttpRequestMethodNotSupportedException", ex);
    ExceptionResponse exceptionResponse = ExceptionResponse.fromOverrideMessage(ex,
        req.getRequestURI());
    return ResponseEntity.status(HttpStatusCode.valueOf(405)).body(exceptionResponse);
  }

  @ExceptionHandler(InvalidRequestException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<?> handleInvalidRequestException(final HttpServletRequest req,
      final InvalidRequestException ex) {
    LOGGER.info("Request not valid: {}", ex.getMessage());
    ExceptionResponse exceptionResponse = ExceptionResponse.from(ex, req.getRequestURI());
    return ResponseEntity.badRequest().body(exceptionResponse);
  }

  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<?> handleNotFound(final HttpServletRequest req,
      final NotFoundException ex) {
    LOGGER.info("Resource not found: {}", ex.getMessage());
    ExceptionResponse exceptionResponse = ExceptionResponse.from(ex, req.getRequestURI());
    return ResponseEntity.status(HttpStatusCode.valueOf(404)).body(exceptionResponse);
  }

  @ExceptionHandler(DataViolationException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ResponseEntity<?> handleDataViolation(final HttpServletRequest req,
      final DataViolationException ex) {
    LOGGER.warn("Data violation: {}", ex.getMessage());
    ExceptionResponse exceptionResponse = ExceptionResponse.from(ex, req.getRequestURI());
    return ResponseEntity.status(HttpStatusCode.valueOf(409)).body(exceptionResponse);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<?> handleDataIntegrityViolationException(final HttpServletRequest req,
      final DataIntegrityViolationException ex) {
    LOGGER.warn("An unexpected error occurred", ex);
    ExceptionResponse exceptionResponse = ExceptionResponse.fromOverrideMessage(ex,
        req.getRequestURI());
    return ResponseEntity.internalServerError().body(exceptionResponse);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<?> handleGeneralException(final HttpServletRequest req,
      final Exception ex) {
    LOGGER.warn("An unexpected error occurred", ex);
    ExceptionResponse exceptionResponse = ExceptionResponse.fromOverrideMessage(ex,
        req.getRequestURI());
    return ResponseEntity.internalServerError().body(exceptionResponse);
  }
}
