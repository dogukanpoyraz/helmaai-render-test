package com.backend.helmaaibackend.repository;

import com.backend.helmaaibackend.domain.UserAccount;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserAccountRepository extends MongoRepository<UserAccount, String> {
    boolean existsByEmail(String email);
    Optional<UserAccount> findByEmail(String email);
}
