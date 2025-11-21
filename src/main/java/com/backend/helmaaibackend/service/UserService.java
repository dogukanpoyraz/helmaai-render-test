package com.backend.helmaaibackend.service;

import com.backend.helmaaibackend.dto.*;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserView toView(String userId);

    UserView updateProfile(String userId, UpdateProfileRequest request);
    void updatePassword(String userId, UpdatePasswordRequest request);
    UserView updateEmergencyContacts(String userId, UpdateEmergencyContactsRequest request);

    void deactivateAccount(String userId);

    UserView updateProfilePhoto(String userId, MultipartFile file);

    UserView deleteProfilePhoto(String userId);
}
