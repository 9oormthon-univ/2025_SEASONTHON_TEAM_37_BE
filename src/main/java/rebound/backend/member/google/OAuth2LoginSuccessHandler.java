package rebound.backend.member.google;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import rebound.backend.member.domain.Member;
import rebound.backend.member.repository.MemberRepository;
import rebound.backend.member.util.JwtUtil;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // OAuth2User에서 사용자 정보 추출
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // CustomOAuth2UserService에서 사용한 OAuthAttributes의 loginId(Google의 sub)를 가져옴
        String loginId = oAuth2User.getName();

        // loginId로 우리 DB에서 Member를 찾음
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // memberId로 JWT 생성
        String accessToken = jwtUtil.createToken(member.getId());

        // 프론트엔드로 리디렉션할 URL 생성
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth/redirect") // 프론트엔드 주소
                .queryParam("accessToken", accessToken)
                .build().toUriString();

        // 생성된 URL로 리디렉션
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
