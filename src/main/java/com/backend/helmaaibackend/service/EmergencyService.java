package com.backend.helmaaibackend.service;

import com.backend.helmaaibackend.domain.UserAccount;
import com.backend.helmaaibackend.dto.EmergencyTriggerRequest;
import com.backend.helmaaibackend.dto.EmergencyTriggerResponse;

public interface EmergencyService {
    EmergencyTriggerResponse trigger(UserAccount currentUser, EmergencyTriggerRequest req);
}
