package com.backend.helmaaibackend.exception;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

/**
 * Global exception handler.
 * Swagger dokümantasyonuna karışmaması için @Hidden.
 * @ControllerAdvice + @ResponseBody -> JSON error response döner.
 */
@Hidden
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    // 400 - bad input (ör: eski şifre yanlış)
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST) // 400
                .body(Map.of(
                        "timestamp", Instant.now().toString(),
                        "error", "bad_request",
                        "message", ex.getMessage()
                ));
    }

    // 409 - business conflict (örn email zaten kayıtlı)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT) // 409
                .body(Map.of(
                        "timestamp", Instant.now().toString(),
                        "error", "conflict",
                        "message", ex.getMessage()
                ));
    }

    // 423 - kullanıcı aktif değil vs
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.LOCKED) // 423
                .body(Map.of(
                        "timestamp", Instant.now().toString(),
                        "error", "locked",
                        "message", ex.getMessage()
                ));
    }

    // 404 - bulunamadı
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
