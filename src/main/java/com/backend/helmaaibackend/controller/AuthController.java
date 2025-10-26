package com.backend.helmaaibackend.controller;

import com.backend.helmaaibackend.dto.AuthResponse;
import com.backend.helmaaibackend.dto.LoginRequest;
import com.backend.helmaaibackend.dto.RegisterRequest;
import com.backend.helmaaibackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Kayıt ve giriş işlemleri")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(
            summary = "Kullanıcı kaydı",
            description = "Yeni kullanıcı oluşturur. Başarılı olduğunda JWT ve kullanıcı profilini döner.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Kayıt başarılı",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Geçersiz istek gövdesi", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Kullanıcı adı/e-posta zaten kayıtlı", content = @Content)
            }
    )
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @Operation(
            summary = "Giriş yap",
            description = "Kullanıcı adı/e-posta ve parola ile giriş yapar. Başarılı olduğunda JWT ve kullanıcı profilini döner.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Giriş başarılı",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Geçersiz kimlik bilgileri veya istek gövdesi", content = @Content),
                    @ApiResponse(responseCode = "423", description = "Kullanıcı aktif değil", content = @Content)
            }
    )
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }
}
