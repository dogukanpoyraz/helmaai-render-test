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

@Tag(name = "Authentication", description = "Registration and login operations")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(
            summary = "User registration",
            description = "Creates a new user. Returns JWT and user profile on success.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Registration successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
                    @ApiResponse(responseCode = "409", description = "Username/email already registered", content = @Content)
            }
    )
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @Operation(
            summary = "Login",
            description = "Logs in with username/email and password. Returns JWT and user profile on success.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AuthResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid credentials or request body", content = @Content),
                    @ApiResponse(responseCode = "423", description = "User is not active", content = @Content)
            }
    )
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }
}
