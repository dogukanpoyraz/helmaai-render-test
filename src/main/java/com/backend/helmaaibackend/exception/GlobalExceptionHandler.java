package com.backend.helmaaibackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Hidden;

import java.time.Instant;
import java.util.Map;

/**
 * Global exception handler.
 * Bu sınıf Swagger dokümantasyonunda görünmesin diye @Hidden ekliyoruz.
 * Ayrıca @RestControllerAdvice yerine @ControllerAdvice + @ResponseBody kullanıyoruz;
 * bazı springdoc sürümlerinde bu daha uyumlu.
 */
@Hidden
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    // email zaten kayıtlı vb. business logic hataları
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT) // 409
                .body(Map.of(
                        "timestamp", Instant.now().toString(),
                        "error", "conflict",
                        "message", ex.getMessage()
                ));
    }

    // kullanıcı aktif değil gibi durumlar
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.LOCKED) // 423
                .body(Map.of(
                        "timestamp", Instant.now().toString(),
                        "error", "locked",
                        "message", ex.getMessage()
                ));
    }

    // bulunamadı senaryoları -> örn. ileride /api/me/{id} gibi durumlar
    @ExceptionHandler(java.util.NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(java.util.NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND) // 404
                .body(Map.of(
                        "timestamp", Instant.now().toString(),
                        "error", "not_found",
                        "message", ex.getMessage()
                ));
    }
}
