package com.backend.helmaaibackend.service;

import com.backend.helmaaibackend.dto.*;

public interface UserService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserView toView(String userId);
}
