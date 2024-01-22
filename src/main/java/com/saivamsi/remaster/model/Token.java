package com.saivamsi.remaster.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String token;
    private String type;
    @Column(columnDefinition = "boolean default false")
    private boolean expired;
    @Column(columnDefinition = "boolean default false")
    private boolean revoked;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private ApplicationUser user;
}
