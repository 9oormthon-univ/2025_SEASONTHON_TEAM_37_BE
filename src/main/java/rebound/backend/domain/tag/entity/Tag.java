package rebound.backend.domain.tag.entity;

import jakarta.persistence.*;
import lombok.*;
import rebound.backend.domain.post.entity.Post;

@Entity
@Table(name = "tag")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long tagId;

    @Column(name = "name", nullable = false, length = 50,unique=true)
    private String name;

    //역방향 참조 -> 태그에서 해당 태그가 달린 게시글 목록을 가져오고 싶을 때 사용
    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private java.util.Set<Post> posts = new java.util.LinkedHashSet<>();
}
