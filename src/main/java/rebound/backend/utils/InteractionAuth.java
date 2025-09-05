package rebound.backend.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;
import java.util.Map;

public final class InteractionAuth {

    private InteractionAuth() {}

    public static Long currentMemberId() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated()) {
            throw new IllegalStateException("Unauthenticated");
        }

        Object p = a.getPrincipal();

        // 1) Spring Security Resource Server 형태(Jwt가 principal)
        if (p instanceof Jwt jwt) {
            String sub = jwt.getSubject();
            if (!StringUtils.hasText(sub)) {
                Object legacy = jwt.getClaims().get("memberId"); // 과거 방식 폴백
                if (legacy != null) sub = String.valueOf(legacy);
            }
            if (StringUtils.hasText(sub)) return Long.parseLong(sub);
        }

        // 2) 우리가 커스텀 필터에서 Long/String을 principal로 넣는 경우
        if (p instanceof Long l) return l;
        if (p instanceof String s && StringUtils.hasText(s)) return Long.parseLong(s);

        // 3) UserDetails로 들어오는 경우(username에 memberId를 넣었다면)
        if (p instanceof UserDetails u && StringUtils.hasText(u.getUsername())) {
            return Long.parseLong(u.getUsername());
        }

        // 4) 일부 구현체는 Map을 principal로 줄 수 있음
        if (p instanceof Map<?,?> map) {
            Object sub = map.get("sub");
            if (sub == null) sub = map.get("memberId");
            if (sub != null) return Long.parseLong(String.valueOf(sub));
        }

        throw new IllegalStateException("Cannot resolve memberId from principal: " + p);
    }
}