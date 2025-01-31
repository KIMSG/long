package com.longleg.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "조회 정보를 나타내는 엔티티")
@Entity
@Getter
//@Setter
//@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class UserActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_id", nullable = false)
    private Work work;

    @Enumerated(EnumType.STRING)  // ✅ ENUM을 String으로 저장
    @Column(name = "activity_type", nullable = false)
    private ActivityType activityType;

    private boolean isActive = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.activityType == null) {
            this.activityType = ActivityType.VIEW;  // ✅ 기본값 설정
        }
    }

    @Builder
    public UserActivity(User user, Work work, ActivityType activityType) {
        this.user = user;
        this.work = work;
        this.activityType = (activityType != null) ? activityType : ActivityType.VIEW;  // ✅ ENUM 값으로 설정
        this.createdAt = LocalDateTime.now();
    }
}

