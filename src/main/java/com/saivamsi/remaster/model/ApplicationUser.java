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
    private boolean verified;
    @Enumerated(EnumType.STRING)
    private Role role;
    @OneToMany(mappedBy = "user")
    private List<Token> tokens;
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Remaster> remasters;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;
    @CreationTimestamp
    @Column(name = "created_at")
    private Date createdAt;

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
                .name(this.name).bio(this.bio).image(this.image).role(this.role).verified(this.verified).build();
    }

    public BasicUserResponse getBasicUser() {
        return BasicUserResponse.builder().id(this.id).username(this.username).build();
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
