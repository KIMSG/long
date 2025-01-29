package com.longleg.reward.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "작품 정보를 나타내는 엔티티")
@Entity
@Getter
@Setter
@ToString
public class Work {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    private int viewCount;

    private int likeCount;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public void increaseViewCount() {
        this.viewCount++;
    }
}
