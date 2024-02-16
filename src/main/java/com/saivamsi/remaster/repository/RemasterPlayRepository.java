package com.saivamsi.remaster.repository;

import com.saivamsi.remaster.model.Remaster;
import com.saivamsi.remaster.model.RemasterPlay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RemasterPlayRepository extends JpaRepository<RemasterPlay, UUID> {

    @Query("""
            select p from RemasterPlay p where p.remaster.id = :remasterId and p.user.id = :userId
            """)
    Optional<RemasterPlay> findByRemasterIdAndUserId(UUID remasterId, UUID userId);
}
