package com.backend.helmaaibackend.service;

import com.backend.helmaaibackend.dto.*;

public interface UserService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserView toView(String userId);

    UserView updateProfile(String userId, UpdateProfileRequest request);
    void updatePassword(String userId, UpdatePasswordRequest request);
    UserView updateEmergencyContacts(String userId, UpdateEmergencyContactsRequest request);

    void deactivateAccount(String userId); // new
}
