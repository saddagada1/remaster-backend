package com.saivamsi.remaster.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RemasterLike {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "remaster_id")
    private Remaster remaster;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private ApplicationUser user;
    @CreationTimestamp
    @Column(name = "created_at")
    private Date createdAt;
}
