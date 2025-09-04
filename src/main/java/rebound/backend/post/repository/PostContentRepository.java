package rebound.backend.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rebound.backend.post.entity.PostContent;

public interface PostContentRepository extends JpaRepository<PostContent, Long> {
}
