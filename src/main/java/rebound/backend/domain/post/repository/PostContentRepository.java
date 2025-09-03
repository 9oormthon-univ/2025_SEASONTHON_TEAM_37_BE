package rebound.backend.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rebound.backend.domain.post.entity.PostContent;

public interface PostContentRepository extends JpaRepository<PostContent, Long> {
}
