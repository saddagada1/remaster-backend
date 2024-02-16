package com.saivamsi.remaster.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RemasterPlay {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column()
    private Integer count;
    @ManyToOne
    @JoinColumn(name = "remaster_id")
    private Remaster remaster;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private ApplicationUser user;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;
    @CreationTimestamp
    @Column(name = "created_at")
    private Date createdAt;

    public RemasterPlay incrementCount() {
        this.count += 1;
        return this;
    }
}
