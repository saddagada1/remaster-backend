package com.saivamsi.remaster.repository;

import com.saivamsi.remaster.model.Session;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    @Query("""
            select s from Session s inner join ApplicationUser u on s.user.id = u.id
            where u.id = :userId
            """)
    List<Session> findAllSessionsByUser(UUID userId);
    @Transactional
    @Modifying
    @Query("""
            delete from Session s where s.user.id in (select u.id from ApplicationUser u where u.id = :userId)
            """)
    void deleteAllSessionsByUser(UUID userId);
    @Transactional
    @Modifying
    void deleteByAccessToken(String token);
    @Transactional
    @Modifying
    void deleteByRefreshToken(String token);
    Boolean existsByAccessToken(String token);
    Boolean existsByRefreshToken(String token);
}
