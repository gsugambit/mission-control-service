package com.gambit.labs.mission.control.controller;

import com.gambit.labs.mission.control.dto.ErrorResponseDto;
import com.gambit.labs.mission.control.exception.DataViolationException;
import com.gambit.labs.mission.control.exception.ExceptionResponse;
import com.gambit.labs.mission.control.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
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

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleMethodArgumentNotValidException(HttpServletRequest req,
      MethodArgumentNotValidException ex) {
    LOGGER.info("MethodArgumentNotValidException", ex);
    ExceptionResponse exceptionResponse = ExceptionResponse.from(ex, req.getRequestURI());
    final String methodExceptionString = ex.getBindingResult().getAllErrors().stream()
        .map((error) -> {
          return "field: " + ((FieldError) error).getField() + " rule: " + error.getDefaultMessage()
              + " violated with value: " + ((FieldError) error).getRejectedValue();
        }).collect(Collectors.joining(", "));
    exceptionResponse.setMessage(methodExceptionString);

    return ResponseEntity.status(HttpStatusCode.valueOf(400)).body(exceptionResponse);
  }

  @ExceptionHandler(MissingServletRequestPartException.class)
  public ResponseEntity<?> handleMissingServletRequestPartException(HttpServletRequest req,
      MissingServletRequestPartException ex) {
    LOGGER.info("MissingServletRequestPartException", ex);
    ExceptionResponse exceptionResponse = ExceptionResponse.from(ex, req.getRequestURI());

    return ResponseEntity.status(HttpStatusCode.valueOf(400)).body(exceptionResponse);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<?> handleMissingServletRequestParameterException(HttpServletRequest req,
      MissingServletRequestParameterException ex) {
    LOGGER.info("MissingServletRequestParameterException", ex);
    ExceptionResponse exceptionResponse = ExceptionResponse.from(ex, req.getRequestURI());

    return ResponseEntity.status(HttpStatusCode.valueOf(400)).body(exceptionResponse);
  }

  @ExceptionHandler(PropertyReferenceException.class)
  public ResponseEntity<?> handlePropertyReferenceException(HttpServletRequest req,
      PropertyReferenceException ex) {
    LOGGER.info("PropertyReferenceException", ex);
    ExceptionResponse exceptionResponse = ExceptionResponse.from(ex, req.getRequestURI());

    return ResponseEntity.status(HttpStatusCode.valueOf(400)).body(exceptionResponse);
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
    exceptionResponse.setMessage("An unexpected error occurred. Please try again later.");

    return ResponseEntity.status(HttpStatusCode.valueOf(400)).body(exceptionResponse);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<?> handleHttpRequestMethodNotSupportedException(HttpServletRequest req,
      HttpRequestMethodNotSupportedException ex) {
    LOGGER.info("Unexpected HttpRequestMethodNotSupportedException", ex);
    ExceptionResponse exceptionResponse = ExceptionResponse.from(ex, req.getRequestURI());
    exceptionResponse.setMessage("An unexpected error occurred. Please try again later.");

    return ResponseEntity.status(HttpStatusCode.valueOf(404)).body(exceptionResponse);
  }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDto handleNotFound(final NotFoundException ex) {
        LOGGER.warn("Resource not found: {}", ex.getMessage());
        return ErrorResponseDto.builder()
                .withStatus(HttpStatus.NOT_FOUND.value())
                .withError(HttpStatus.NOT_FOUND.getReasonPhrase())
                .withMessage(ex.getMessage())
                .withTimestamp(Instant.now())
                .build();
    }

    @ExceptionHandler(DataViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDto handleDataViolation(final DataViolationException ex) {
        LOGGER.warn("Data violation: {}", ex.getMessage());
        return ErrorResponseDto.builder()
                .withStatus(HttpStatus.CONFLICT.value())
                .withError(HttpStatus.CONFLICT.getReasonPhrase())
                .withMessage(ex.getMessage())
                .withTimestamp(Instant.now())
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDto handleGeneralException(final Exception ex) {
        LOGGER.error("An unexpected error occurred", ex);
        return ErrorResponseDto.builder()
                .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .withError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .withMessage("An unexpected error occurred")
                .withTimestamp(Instant.now())
                .build();
    }
}
