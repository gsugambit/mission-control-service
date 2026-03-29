package com.gambit.labs.mission.control.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gambit.labs.mission.control.utils.ObjectUtils;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;

@Builder
@Data
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor(access = lombok.AccessLevel.PACKAGE)
public class ExceptionResponse {

  public static final String OVERRIDE_EXCEPTION_MESSAGE = "An unexpected error occurred. Please try again later.";

  private Instant timestamp;
  private String causeExceptionClass;
  private String causeExceptionClassName;
  private String causeExceptionMessage;
  private String exceptionClass;
  private String exceptionClassName;
  private String message;
  private String path;
  private String traceId;
  private String spanId;

  public static ExceptionResponse from(final Exception ex, final String path) {
    String traceId = MDC.get("traceId");
    String spanId = MDC.get("spanId");

    ExceptionResponse.ExceptionResponseBuilder builder = ExceptionResponse.builder()
        .exceptionClass(ex.getClass().getName())
        .exceptionClassName(ex.getClass().getSimpleName())
        .message(ex.getMessage())
        .timestamp(Instant.now())
        .path(path)
        .traceId(traceId)
        .spanId(spanId);

    Throwable cause = ex.getCause();
    if (cause != null) {
      builder.causeExceptionClass(ex.getClass().getName())
          .causeExceptionClassName(ex.getClass().getSimpleName())
          .causeExceptionMessage(cause.getMessage());
    }

    return builder.build();
  }

  public static ExceptionResponse fromOverrideMessage(final Exception ex, final String path) {
    String traceId = MDC.get("traceId");
    String spanId = MDC.get("spanId");

    ExceptionResponse.ExceptionResponseBuilder builder = ExceptionResponse.builder()
        .exceptionClass(ex.getClass().getName())
        .exceptionClassName(ex.getClass().getSimpleName())
        .message(OVERRIDE_EXCEPTION_MESSAGE)
        .timestamp(Instant.now())
        .path(path)
        .traceId(traceId)
        .spanId(spanId);

    Throwable cause = ex.getCause();
    if (cause != null) {
      builder.causeExceptionClass(ex.getClass().getName())
          .causeExceptionClassName(ex.getClass().getSimpleName())
          .causeExceptionMessage(OVERRIDE_EXCEPTION_MESSAGE);
    }

    return builder.build();
  }

  @Override
  public String toString() {
    return ObjectUtils.toString(this);
  }
}
