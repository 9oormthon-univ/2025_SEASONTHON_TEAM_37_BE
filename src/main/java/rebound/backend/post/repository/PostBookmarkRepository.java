package rebound.backend.post.repository;

import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rebound.backend.post.entity.PostBookmark;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Collection;

public interface PostBookmarkRepository extends JpaRepository<PostBookmark, Long> {

    boolean existsByPostIdAndMemberId(Long postId, Long memberId);

    long deleteByPostIdAndMemberId(Long postId, Long memberId);

    long countByPostId(Long postId); // 최신 집계 반환 (목록/버튼 옆 숫자 표시용)

    //마이페이지: 내가 스크랩한 게시글 ID목록(최신순)
    @Query("""
           select b.postId
           from PostBookmark b
           where b.memberId = :memberId
           order by b.createdAt desc
           """)
    Slice<Long> findBookmarkedPostIdsBy(Long memberId, org.springframework.data.domain.Pageable pageable);
    // 마이페이지: 내가 북마크한 글 postId 목록 (최신순)
    @Query("""
           select b.postId
           from PostBookmark b
           where b.memberId = :memberId
           order by b.createdAt desc
           """)
    List<Long> findPostIdsBookmarkedBy(@Param("memberId") Long memberId);

    // 특정 여러 게시글(postIds)에 대해서 북마크 여부 확인
    @Query("""
           select b.postId
           from PostBookmark b
           where b.memberId = :memberId and b.postId in :postIds
           """)
    List<Long> findBookmarkedPostIds(@Param("memberId") Long memberId,
                                     @Param("postIds") Collection<Long> postIds);
}
