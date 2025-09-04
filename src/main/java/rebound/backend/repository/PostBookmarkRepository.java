package rebound.backend.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import rebound.backend.entity.PostBookmark;

public interface PostBookmarkRepository extends JpaRepository<PostBookmark, Long> {
    boolean existsByPostIdAndMemberId(Long postId, Long memberId);
    long deleteByPostIdAndMemberId(Long postId, Long memberId);
    long countByPostId(Long postId);
}
