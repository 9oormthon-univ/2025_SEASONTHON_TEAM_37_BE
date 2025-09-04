package rebound.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public final class AuthUtils {

    private AuthUtils() {}

    public static Long currentMemberId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException();
        }

        // 1) JWT(Resource Server) 방식
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();

            // 우선순위: memberId → sub
            Long id = coerceToLong(jwt.getClaim("memberId"));
            if (id == null) {
                id = coerceToLong(jwt.getClaim("sub"));
            }
            if (id == null) {
                // sub가 이메일/UUID일 수도 있으므로, 이 경우엔 서비스 정책에 맞게 변환 규칙을 정해야 함
                // 예: sub가 "123"처럼 숫자 문자열인 경우만 허용
                throw new UnauthorizedException();
            }
            return id;
        }

        // 2) UserDetails 기반 (폼 로그인/세션 등)
        Object principal = auth.getPrincipal();

        // 2-a) 커스텀 Principal이 있고 getId() 제공 시
        if (principal instanceof HasIdPrincipal p) {
            return p.getId();
        }

        // 2-b) 스프링 기본 UserDetails라면 username을 Long으로 파싱 (정책에 맞게)
        if (principal instanceof UserDetails ud) {
            Long id = tryParseLong(ud.getUsername());
            if (id != null) return id;
        }

        throw new UnauthorizedException();
    }

    /** JWT 클레임(Object)을 안전하게 Long으로 변환 */
    private static Long coerceToLong(Object claim) {
        if (claim == null) return null;
        if (claim instanceof Number n) return n.longValue();
        if (claim instanceof String s) {
            return tryParseLong(s);
        }
        return null;
    }

    private static Long tryParseLong(String s) {
        try {
            return (s == null || s.isBlank()) ? null : Long.parseLong(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 커스텀 Principal을 쓰는 경우(예: UserPrincipal 등) 이 인터페이스를 구현해두면
     * AuthUtils가 공통적으로 getId()를 통해 ID를 꺼낼 수 있어요.
     */
    public interface HasIdPrincipal {
        Long getId();
    }
}
