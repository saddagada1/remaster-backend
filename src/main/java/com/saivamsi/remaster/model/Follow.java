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
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "followed_id")
    private ApplicationUser followed;
    @ManyToOne
    @JoinColumn(name = "follower_id")
    private ApplicationUser follower;
    @CreationTimestamp
    @Column(name = "created_at")
    private Date createdAt;
}
