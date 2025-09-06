package rebound.backend.member.google.dtos;

import lombok.Builder;
import lombok.Getter;
import rebound.backend.member.domain.Member;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String loginId;
    private String name;

    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        return ofGoogle(userNameAttributeName, attributes);
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .loginId((String) attributes.get("sub"))
                .name((String) attributes.get("name"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    // Member 엔티티를 생성하는 메서드
    public Member toEntity() {
        return Member.builder()
                .loginId(loginId)         // loginId 필드에 Google의 'sub' 값 저장
                .nickname(name)           // nickname 필드에 Google의 'name' 값 저장
                .password_hash(null)      // 소셜 로그인이므로 비밀번호는 null
                .provider("GOOGLE")
                .age(20)                  //구글 로그인시 나이는 일단 20으로 default 세팅 -> 마이페이지에서 변경하도록
                .createdAt(LocalDateTime.now())
                .build();
    }
}
