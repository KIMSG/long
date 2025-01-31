package com.longleg.entity;

import jakarta.persistence.*;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "작품 정보를 나타내는 엔티티")
@Entity
@Getter
//@Setter
@ToString(exclude = "author") // 순환 참조 방지
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 보호
@EqualsAndHashCode(of = "id")
@Table(name = "works")
public class Work {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

//    @Column(name = "author_id", nullable = false)
//    private Long authorId;
    @ManyToOne(fetch = FetchType.LAZY) // authorId를 User 엔티티와 매핑
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false)
    private int viewCount = 0;

    @Column(nullable = false)
    private int likeCount = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // 필수 필드만 포함한 생성자
    @Builder
    public Work(String title, User author) {
        this.title = title;
        this.author = author;
        this.viewCount = 0;
        this.likeCount = 0;
    }

    // 조회수 증가 메서드
    public void increaseViewCount() {
        this.viewCount++;
    }

    // 좋아요 증가 메서드
    public void increaseLikeCount() {
        this.likeCount++;
    }

    @PrePersist  // 🚀 JPA에서 자동으로 createdAt 설정
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;  // ✅ 좋아요 수 감소 (음수 방지)
        }
    }
}

/*
*
*3. 개선된 코드 설명
✅ 1) 엔티티 이름 변경 (Works → Work)
단수형 명명 규칙을 따름 → Work
JpaRepository<Work, Long> 같은 레포지토리에서도 가독성이 좋아짐.
✅ 2) authorId를 User 엔티티와 @ManyToOne 관계 설정
작가 정보를 직접 조회 가능 (Lazy Loading 사용)
Long authorId 대신 User author 사용
@JoinColumn(name = "author_id")를 지정해서 기존 author_id 컬럼 유지
✅ 3) @Setter 제거 (불변성 유지)
title과 author는 생성자에서 설정하고 변경 불가능
viewCount, likeCount는 메서드(increaseViewCount, increaseLikeCount)를 통해 변경
✅ 4) @ToString(exclude = "author") 추가
무한 루프 방지 (User와 양방향 관계를 가질 경우 순환 참조 가능)
✅ 5) @Builder 추가
빌더 패턴 제공 → new Work() 대신 Work.builder().title("제목").author(user).build(); 사용 가능
*
* */