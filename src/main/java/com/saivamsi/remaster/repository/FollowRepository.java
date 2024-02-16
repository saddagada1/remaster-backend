package com.saivamsi.remaster.repository;

import com.saivamsi.remaster.model.Follow;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<Follow, UUID> {

    @Transactional
    @Modifying
    @Query("""
            delete from Follow f where f.followed.id in (select fd.id from ApplicationUser fd where fd.id = :followedId) and f.follower.id in (select fr.id from ApplicationUser fr where fr.id = :followerId)
            """)
    void deleteByFollowedIdAndFollowerId(UUID followedId, UUID followerId);

    @Query("""
            select f from Follow f where f.followed.id = :followedId and f.follower.id = :followerId
            """)
    Optional<Follow> findByFollowedIdAndFollowerId(UUID followedId, UUID followerId);
}
