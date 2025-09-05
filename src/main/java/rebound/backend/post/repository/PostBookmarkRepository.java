package rebound.backend.post.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rebound.backend.post.entity.PostBookmark;

public interface PostBookmarkRepository extends JpaRepository<PostBookmark, Long> {
    boolean existsByPostIdAndMemberId(Long postId, Long memberId);
    long deleteByPostIdAndMemberId(Long postId, Long memberId);
    long countByPostId(Long postId);

    // 마이페이지: 내가 스크랩한 글 postId 목록 (최신순)
    @Query("""
           select b.postId
           from PostBookmark b
           where b.memberId = :memberId
           order by b.createdAt desc
           """)
    Slice<Long> findBookmarkedPostIdsBy(@Param("memberId") Long memberId,
                                        Pageable pageable);
}
