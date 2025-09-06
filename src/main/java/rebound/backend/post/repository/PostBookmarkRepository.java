package rebound.backend.post.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rebound.backend.post.entity.PostBookmark;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PostBookmarkRepository extends JpaRepository<PostBookmark, Long> {

    boolean existsByPostIdAndMemberId(Long postId, Long memberId);

    long countByPostId(Long postId);

    // N+1 쿼리 해결: 여러 게시글의 북마크 수를 한 번에 조회하는 메서드
    @Query("SELECT pb.postId, COUNT(pb) FROM PostBookmark pb WHERE pb.postId IN :postIds GROUP BY pb.postId")
    Map<Long, Long> countByPostIds(@Param("postIds") List<Long> postIds);

    // N+1 쿼리 해결: 현재 사용자가 북마크한 게시글 ID를 한 번에 조회하는 메서드
    @Query("SELECT pb.postId FROM PostBookmark pb WHERE pb.memberId = :memberId AND pb.postId IN :postIds")
    Set<Long> findBookmarkedPostIdsByMemberIdAndPostIds(@Param("memberId") Long memberId, @Param("postIds") List<Long> postIds);

    // 추가해야 할 메서드 2: 특정 게시글에 대한 특정 사용자의 북마크 삭제
    @Transactional
    long deleteByPostIdAndMemberId(Long postId, Long memberId);

    @Query("SELECT pb.postId FROM PostBookmark pb WHERE pb.memberId = :memberId ORDER BY pb.bookmarkedAt DESC")
    Slice<Long> findBookmarkedPostIdsBy(@Param("memberId") Long memberId, Pageable pageable);
}
