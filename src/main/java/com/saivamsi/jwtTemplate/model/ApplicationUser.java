package com.saivamsi.jwtTemplate.model;

import com.saivamsi.jwtTemplate.response.UserResponse;
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
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;
    @CreationTimestamp
    @Column(name = "created_at")
    private Date createdAt;

    public UserResponse getSafeUser() {
        return UserResponse.builder().id(this.id).username(this.username).email(this.email)
                .name(this.name).bio(this.bio).image(this.image).role(this.role).verified(this.verified).updatedAt(this.updatedAt).createdAt(this.createdAt).build();
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
