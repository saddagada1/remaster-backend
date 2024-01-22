package com.saivamsi.remaster.model;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.saivamsi.remaster.response.RemasterResponse;
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
public class Remaster {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column()
    private String url;
    @Column()
    private String name;
    @Column(nullable = true, columnDefinition = "text")
    private String description;
    @Column()
    private Float duration;
    @Column(columnDefinition = "integer default 0")
    private Integer key;
    @Column(columnDefinition = "integer default 0")
    private Integer mode;
    @Column(columnDefinition = "float default 80.0")
    private Float tempo;
    @Column(columnDefinition = "integer default 3")
    private Integer timeSignature;
    @Column(columnDefinition = "integer default 0")
    private Integer tuning;
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIncludeProperties({ "id", "username" })
    private ApplicationUser user;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;
    @CreationTimestamp
    @Column(name = "created_at")
    private Date createdAt;

    public RemasterResponse getRemasterResponse() {
        return RemasterResponse.builder().id(this.id).url(this.url).name(this.name).description(this.description).duration(this.duration).key(this.key)
                .mode(this.mode).tempo(this.tempo).timeSignature(this.timeSignature).tuning(this.tuning).user(this.user.getBasicUser()).updatedAt(this.updatedAt)
                .createdAt(this.createdAt).build();
    }
}
