package rebound.backend.post.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import rebound.backend.post.entity.PostBookmark;

public interface PostBookmarkRepository extends JpaRepository<PostBookmark, Long> {
    boolean existsByPostIdAndMemberId(Long postId, Long memberId);
    long deleteByPostIdAndMemberId(Long postId, Long memberId);
    long countByPostId(Long postId);
}
