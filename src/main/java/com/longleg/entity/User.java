package com.longleg.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "사용자/작가 정보를 나타내는 엔티티")
@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole userRole;

    private long reward = 0L;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();


    @Builder
    public User(String name, UserRole userRole) {
        this.name = name;
        this.userRole = userRole;
    }

    public void addReward(long points) {
        this.reward += points;
    }

    @PrePersist  // 🚀 JPA에서 자동으로 createdAt 설정
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}

