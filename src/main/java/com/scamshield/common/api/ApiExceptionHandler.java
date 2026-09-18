package com.scamshield.common.api;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(IllegalArgumentException.class)
  ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException exception) {
    return ResponseEntity.badRequest().body(Map.of(
        "error",
        Map.of("code", "BAD_REQUEST", "message", exception.getMessage())));
  }

  @ExceptionHandler(ResponseStatusException.class)
  ResponseEntity<Map<String, Object>> status(ResponseStatusException exception) {
    return ResponseEntity.status(exception.getStatusCode()).body(Map.of(
        "error",
        Map.of(
            "code", exception.getStatusCode().toString(),
            "message", exception.getReason())));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<Map<String, Object>> malformedJson() {
    return ResponseEntity.badRequest().body(Map.of(
        "error",
        Map.of("code", "INVALID_JSON", "message", "Request body must be valid JSON")));
  }
}
