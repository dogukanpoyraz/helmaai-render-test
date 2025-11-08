package com.backend.helmaaibackend.repository;

import com.backend.helmaaibackend.domain.Role;
import com.backend.helmaaibackend.domain.UserAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface UserAccountRepository extends MongoRepository<UserAccount, String> {
    boolean existsByEmail(String email);
    Optional<UserAccount> findByEmail(String email);

    Page<UserAccount> findByDeletedAtIsNull(Pageable pageable);

    @Query("{ 'deletedAt': null, $or: [ {'email': { $regex: ?0, $options: 'i' }}, {'fullName': { $regex: ?0, $options: 'i' }} ] }")
    Page<UserAccount> searchActive(String keyword, Pageable pageable);

    long countByDeletedAtIsNull();
    long countByActiveTrueAndDeletedAtIsNull();
    long countByDeletedAtIsNotNull();

    long countByRolesContainingAndDeletedAtIsNull(Role role);
}
