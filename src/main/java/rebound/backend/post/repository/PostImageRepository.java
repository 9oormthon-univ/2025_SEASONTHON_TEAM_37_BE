package rebound.backend.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rebound.backend.post.entity.PostImage;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
}