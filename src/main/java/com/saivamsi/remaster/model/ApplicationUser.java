package com.saivamsi.remaster.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.saivamsi.remaster.request.UpdateUserRequest;
import com.saivamsi.remaster.response.BasicUserResponse;
import com.saivamsi.remaster.response.UserResponse;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApplicationUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(unique = true, columnDefinition = "citext")
    private String username;
    @Column(unique = true, columnDefinition = "citext")
    private String email;
    private String password;
    @Column(nullable = true)
    private String name;
    @Column(nullable = true, columnDefinition = "text")
    private String bio;
    @Column(nullable = true)
    private String image;
    @Column(columnDefinition = "boolean default false")
    private boolean verified = false;
    @Enumerated(EnumType.STRING)
    private Role role;
    @JsonIgnore
    @OneToMany(mappedBy = "user")
    private List<Session> sessions;
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Remaster> remasters;
    @Column(columnDefinition = "integer default 0")
    private Integer totalRemasters = 0;
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<RemasterLike> likes;
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<RemasterPlay> plays;
    @JsonIgnore
    @OneToMany(mappedBy = "followed", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Follow> followers;
    @JsonIgnore
    @OneToMany(mappedBy = "follower", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Follow> following;
    @Column(columnDefinition = "integer default 0")
    private Integer totalFollowers = 0;
    @Column(columnDefinition = "integer default 0")
    private Integer totalFollowing = 0;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;
    @CreationTimestamp
    @Column(name = "created_at")
    private Date createdAt;

    public ApplicationUser(String username, String email, String password, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public ApplicationUser updateUser(UpdateUserRequest user) {
        this.image = user.getImage();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.name = user.getName();
        this.bio = user.getBio();
        this.password = user.getPassword();

        return this;
    }

    public UserResponse getSafeUser() {
        return UserResponse.builder().id(this.id).username(this.username).email(this.email)
                .name(this.name).bio(this.bio).image(this.image).role(this.role).verified(this.verified)
                .totalRemasters(this.totalRemasters)
                .totalFollowers(this.totalFollowers)
                .totalFollowing(this.totalFollowing)
                .build();
    }

    public BasicUserResponse getBasicUser() {
        return BasicUserResponse.builder().id(this.id).username(this.username).build();
    }

    public ApplicationUser incrementTotalFollowers() {
        this.totalFollowers += 1;
        return this;
    }

    public ApplicationUser decrementTotalFollowers() {
        this.totalFollowers -= 1;
        return this;
    }

    public ApplicationUser incrementTotalFollowing() {
        this.totalFollowing += 1;
        return this;
    }

    public ApplicationUser decrementTotalFollowing() {
        this.totalFollowing -= 1;
        return this;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
