package com.saivamsi.remaster.repository;

import com.saivamsi.remaster.model.Remaster;
import com.saivamsi.remaster.model.RemasterLike;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RemasterLikeRepository extends JpaRepository<RemasterLike, UUID> {

    @Transactional
    @Modifying
    @Query("""
            delete from RemasterLike l where l.remaster.id in (select r.id from Remaster r where r.id = :remasterId) and l.user.id in (select u.id from ApplicationUser u where u.id = :userId)
            """)
    void deleteByRemasterIdAndUserId(UUID remasterId, UUID userId);

    @Query("""
            select l from RemasterLike l where l.remaster.id = :remasterId and l.user.id = :userId
            """)
    Optional<RemasterLike> findByRemasterIdAndUserId(UUID remasterId, UUID userId);

}
