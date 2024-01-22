package com.saivamsi.remaster.repository;

import com.saivamsi.remaster.model.Remaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RemasterRepository extends JpaRepository<Remaster, UUID> {
    @Query(value = """
            select r.* from Remaster r
            join application_user u on r.user_id = u.id
            where u.id = :userId and r.id >= :cursor
            order by r.id asc
            limit :limit
            """, nativeQuery = true)
    List<Remaster> findAllByUserIdAndCursor(UUID userId, UUID cursor, Integer limit);
    @Query(value = """
            select r.* from Remaster r
            join application_user u on r.user_id = u.id
            where u.id = :userId
            order by r.id asc
            limit :limit
            """, nativeQuery = true)
    List<Remaster> findAllByUserId(UUID userId, Integer limit);
    @Query("""
            select r from Remaster r where r.id = :id and r.user.id = :userId
            """)
    Optional<Remaster> findByIdAndUserId(UUID id, UUID userId);
}
