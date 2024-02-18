package com.saivamsi.remaster.repository;

import com.saivamsi.remaster.model.Follow;
import com.saivamsi.remaster.model.RemasterLike;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Query(value = """
            select f.* from follow f
            join application_user u on f.follower_id = u.id
            where u.id = :followerId and f.id >= :cursor
            order by f.id desc
            limit :limit
            """, nativeQuery = true)
    List<Follow> findAllByFollowerIdAndCursor(UUID followerId, UUID cursor, Integer limit);

    @Query(value = """
            select f.* from follow f
            join application_user u on f.follower_id = u.id
            where u.id = :followerId
            order by f.id desc
            limit :limit
            """, nativeQuery = true)
    List<Follow> findAllByFollowerId(UUID followerId, Integer limit);

    @Query(value = """
            select f.* from follow f
            join application_user u on f.followed_id = u.id
            where u.id = :followedId and f.id >= :cursor
            order by f.id desc
            limit :limit
            """, nativeQuery = true)
    List<Follow> findAllByFollowedIdAndCursor(UUID followedId, UUID cursor, Integer limit);

    @Query(value = """
            select f.* from follow f
            join application_user u on f.followed_id = u.id
            where u.id = :followedId
            order by f.id desc
            limit :limit
            """, nativeQuery = true)
    List<Follow> findAllByFollowedId(UUID followedId, Integer limit);
}
