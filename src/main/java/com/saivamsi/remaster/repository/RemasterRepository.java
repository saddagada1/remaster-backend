package com.saivamsi.remaster.repository;

import com.saivamsi.remaster.model.Remaster;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
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

    @Transactional
    @Modifying
    @Query("""
            delete from Remaster r where r.id = :id and r.user.id in (select u.id from ApplicationUser u where u.id = :userId)
            """)
    void deleteByIdAndUserId(UUID id, UUID userId);

    @Query(value = """
            select r.* from Remaster r
            join application_user u on r.user_id = u.id
            where r.id >= :cursor and (levenshtein(r.url, :query) <= 3 or levenshtein(r.name, :query) <= 3 or levenshtein(r.description, :query) <= 3)
            order by r.id asc
            limit :limit
            """, nativeQuery = true)
    List<Remaster> searchRemastersWithCursor(String query, UUID cursor, Integer limit);

    @Query(value = """
            select r.* from Remaster r
            join application_user u on r.user_id = u.id
            where levenshtein(r.url, :query) <= 3 or levenshtein(r.name, :query) <= 3 or levenshtein(r.description, :query) <= 3
            order by r.id asc
            limit :limit
            """, nativeQuery = true)
    List<Remaster> searchRemasters(String query, Integer limit);
}
