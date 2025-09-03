package rebound.backend.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rebound.backend.domain.post.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
}
