package rebound.backend.domain.post.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import rebound.backend.domain.category.entity.MainCategory;
import rebound.backend.domain.category.entity.SubCategory;
import rebound.backend.domain.tag.entity.Tag;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "post")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @NotNull
    @Column(nullable = false, name = "member_id")
    private Long memberId;

    /** 대분류/소분류: Enum(STRING) 저장 */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "main_category", nullable = false, length = 20)
    private MainCategory mainCategory;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "sub_category", nullable = false, length = 40)
    private SubCategory subCategory;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "is_anonymous", nullable = false)
    @Builder.Default
    private Boolean isAnonymous = Boolean.FALSE;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private Status status = Status.DRAFT; // 기본 초안

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    /** 1:1 연관관계 (PostContent가 FK 보유), Post 삭제 시 컨텐츠도 함께 제거 */
    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PostContent postContent;

    public enum Status {
        PUBLIC, HIDDEN, DELETED, DRAFT
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "post_tag",
            joinColumns = @JoinColumn(name = "post_id", foreignKey = @ForeignKey(name="fk_post_tag_post")),
            inverseJoinColumns = @JoinColumn(name = "tag_id", foreignKey = @ForeignKey(name="fk_post_tag_tag")),
            uniqueConstraints = @UniqueConstraint(name = "uq_post_tag", columnNames = {"post_id","tag_id"})
    )
    private Set<Tag> tags = new LinkedHashSet<>();

    /** 카테고리 일관성 보장: 소분류가 대분류에 속하는지 확인 */
    @PrePersist @PreUpdate
    private void validateCategoryConsistency() {
        if (mainCategory == null || subCategory == null) {
            throw new IllegalStateException("카테고리는 필수입니다.");
        }
        if (subCategory.getMainCategory() != mainCategory) {
            throw new IllegalStateException("대분류와 소분류가 일치하지 않습니다.");
        }
        var now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }
}