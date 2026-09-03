package com.cloudbank.auth.repository;

import com.cloudbank.auth.model.AuthUser;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AuthUserRepository extends JpaRepository<AuthUser, UUID> {

    Optional<AuthUser> findByEmailIgnoreCase(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT authUser
            FROM AuthUser authUser
            WHERE LOWER(authUser.email) = LOWER(:email)
            """)
    Optional<AuthUser> findByEmailIgnoreCaseForUpdate(
            @Param("email") String email
    );

}
