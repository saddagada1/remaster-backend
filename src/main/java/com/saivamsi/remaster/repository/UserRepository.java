package com.saivamsi.remaster.repository;

import com.saivamsi.remaster.model.ApplicationUser;
import com.saivamsi.remaster.model.Remaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<ApplicationUser, UUID> {
    Optional<ApplicationUser> findByUsername(String username);

    Optional<ApplicationUser> findByEmail(String email);

    Optional<ApplicationUser> findByUsernameOrEmail(String username, String email);

    @Query(value = """
            select u.* from application_user u
            where u.id >= :cursor and (levenshtein(u.username, :query) <= 3 or levenshtein(u.name, :query) <= 3)
            order by u.id asc
            limit :limit
            """, nativeQuery = true)
    List<ApplicationUser> searchUsersWithCursor(String query, UUID cursor, Integer limit);

    @Query(value = """
            select u.* from application_user u
            where levenshtein(u.username, :query) <= 3 or levenshtein(u.name, :query) <= 3
            order by u.id asc
            limit :limit
            """, nativeQuery = true)
    List<ApplicationUser> searchUsers(String query, Integer limit);
}
