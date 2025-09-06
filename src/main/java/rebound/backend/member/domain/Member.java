package rebound.backend.member.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String nickname;

    private int age;

    private String field;

    @Column(nullable = false)
    private String loginId;

    @Column(nullable = false)
    private String password_hash;

//    private String email;

    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private MemberImage memberImage;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Interest> interests = new ArrayList<>();

    public void addInterest(Interest interest) {
        this.interests.add(interest);
        interest.setMember(this);
    }
}
