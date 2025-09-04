package rebound.backend.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rebound.backend.post.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
}
