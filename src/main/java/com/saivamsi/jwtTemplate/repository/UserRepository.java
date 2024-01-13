package com.saivamsi.jwtTemplate.repository;

import com.saivamsi.jwtTemplate.model.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<ApplicationUser, UUID> {
    Optional<ApplicationUser> findByUsername(String username);

    Optional<ApplicationUser> findByEmail(String email);

    Optional<ApplicationUser> findByUsernameOrEmail(String username, String email);
}
