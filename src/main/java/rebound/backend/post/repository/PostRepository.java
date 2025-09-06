package rebound.backend.post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rebound.backend.post.entity.Post;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    // 최신순
    Page<Post> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    // 인기순 (MVP용 서브쿼리)
    @Query("""
        select p
        from Post p
        where p.status = 'PUBLIC'
          and (:since is null or p.createdAt >= :since)
        order by (
            (select count(r) from PostReaction r
             where r.postId = p.postId
               and r.type = rebound.backend.post.entity.ReactionType.HEART) * 2
          + (select count(b) from PostBookmark b
               where b.postId = p.postId)
          + (select count(c) from Comment c
               where c.postId = p.postId
                 and c.status <> 'DELETED')
        ) desc,
        p.createdAt desc
        """)
    Page<Post> findAllOrderByPopularity(@Param("since") LocalDateTime since,
                                        Pageable pageable);

    @Query("""
    SELECT p
    FROM Post p
    WHERE p.memberId = :memberId
""")
    List<Post> findPostsByMemberId(@Param("memberId") Long memberId);
}
