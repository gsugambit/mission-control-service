package com.gambit.labs.mission.control.exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import com.gambit.labs.mission.control.utils.ObjectUtils;
import org.slf4j.MDC;

import java.time.Instant;

@Builder
@Data
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor(access = lombok.AccessLevel.PACKAGE)
public class ExceptionResponse {
  private Instant timestamp;
  private String exceptionClass;
  private String exceptionClassName;
  private String message;
  private String path;
  private String traceId;
  private String spanId;

  public static ExceptionResponse from(final Exception ex, final String path) {
    String traceId = MDC.get("traceId");
    String spanId = MDC.get("spanId");

    return ExceptionResponse.builder()
        .exceptionClass(ex.getClass().getName())
        .exceptionClassName(ex.getClass().getSimpleName())
        .message(ex.getMessage())
        .timestamp(Instant.now())
        .path(path)
        .traceId(traceId)
        .spanId(spanId)
        .build();
  }

  @Override
  public String toString() {
    return ObjectUtils.toString(this);
  }
}
