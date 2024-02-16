package com.saivamsi.remaster.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String accessToken;
    private String refreshToken;
    @Column()
    private Date accessTokenExpiresAt;
    @Column()
    private Date refreshTokenExpiresAt;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private ApplicationUser user;
}
