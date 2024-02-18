package com.saivamsi.remaster.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.saivamsi.remaster.request.UpdateRemasterRequest;
import com.saivamsi.remaster.response.BasicRemasterResponse;
import com.saivamsi.remaster.response.RemasterResponse;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    @Column(columnDefinition = "float default 0.0")
    private Float duration = 0.0F;
    @Column(columnDefinition = "integer default 0")
    private Integer key = 0;
    @Column(columnDefinition = "integer default 0")
    private Integer mode = 0;
    @Column(columnDefinition = "float default 80.0")
    private Float tempo = 80.0F;
    @Column(columnDefinition = "integer default 3")
    private Integer timeSignature = 3;
    @Column(columnDefinition = "integer default 0")
    private Integer tuning = 0;
    @Column(columnDefinition = "json")
    @ColumnTransformer(write = "?::jsonb")
    private String loops;
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIncludeProperties({ "id", "username", "name", "image" })
    private ApplicationUser user;
    @JsonIgnore
    @OneToMany(mappedBy = "remaster", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<RemasterLike> likes;
    @Column(columnDefinition = "integer default 0")
    private Integer totalLikes = 0;
    @JsonIgnore
    @OneToMany(mappedBy = "remaster", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<RemasterPlay> plays;
    @Column(columnDefinition = "integer default 0")
    private Integer totalPlays = 0;
    @Column()
    private List<Integer> pastLikeCounts = new ArrayList<>();
    @Column()
    private List<Integer> pastPlayCounts = new ArrayList<>();
    @Column(columnDefinition = "float default 0.0")
    private Float likeRank = 0.0F;
    @Column(columnDefinition = "float default 0.0")
    private Float playRank = 0.0F;
    @Column()
    private List<Float> pastLikeRanks = new ArrayList<>();
    @Column()
    private List<Float> pastPlayRanks = new ArrayList<>();
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;
    @CreationTimestamp
    @Column(name = "created_at")
    private Date createdAt;

    public Remaster(String url, String name, String description,
                    Float duration, Integer key, Integer mode,
                    Float tempo, Integer timeSignature, Integer tuning,
                    ApplicationUser user) {
        this.url = url;
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.key = key;
        this.mode = mode;
        this.tempo = tempo;
        this.timeSignature = timeSignature;
        this.tuning = tuning;
        this.user = user;
    }

    public RemasterResponse getRemasterResponse() {
        return RemasterResponse.builder()
                .id(this.id)
                .url(this.url)
                .name(this.name)
                .description(this.description)
                .duration(this.duration)
                .key(this.key)
                .mode(this.mode)
                .tempo(this.tempo)
                .timeSignature(this.timeSignature)
                .tuning(this.tuning)
                .loops(this.loops)
                .user(this.user.getBasicUser())
                .totalLikes(this.totalLikes)
                .totalPlays(this.totalPlays)
                .updatedAt(this.updatedAt)
                .createdAt(this.createdAt).build();
    }

    public BasicRemasterResponse getBasicRemasterResponse() {
        return BasicRemasterResponse.builder()
                .id(this.id)
                .url(this.url)
                .name(this.name)
                .user(this.user.getBasicUser())
                .createdAt(this.createdAt)
                .build();
    }

    public Remaster updateRemaster(UpdateRemasterRequest remaster) {
        this.url = remaster.getUrl();
        this.name = remaster.getName();
        this.description = remaster.getDescription();
        this.duration = remaster.getDuration();
        this.key = remaster.getKey();
        this.mode = remaster.getMode();
        this.tempo = remaster.getTempo();
        this.timeSignature = remaster.getTimeSignature();
        this.tuning = remaster.getTuning();
        this.loops = remaster.getLoops();

        return this;
    }

    public Remaster incrementTotalLikes() {
        this.totalLikes += 1;
        return this;
    }

    public Remaster decrementTotalLikes() {
        this.totalLikes -= 1;
        return this;
    }

    public Remaster incrementTotalPlays() {
        this.totalPlays += 1;
        return this;
    }
}
