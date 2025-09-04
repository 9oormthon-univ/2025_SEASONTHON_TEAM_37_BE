package goorm.rebound.member.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String loginId = null;
        String jwt = null;

        //헤더가 존재하고, "Bearer" 로 시작하는지 확인
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            loginId = jwtUtil.getLoginIdFromToken(jwt);
        }

        //로그인 ID가 존재하고, 현재 SecurityContext에 인증 정보가 없는 경우
        if (loginId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            //토큰 유효 확인
            if (jwtUtil.validateToken(jwt)) {
                //UserDetails 객체 생성(비밀번호 불필요)
                UserDetails userDetails = new User(loginId, "", Collections.emptyList());

                //인증 토큰 생성
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                //SecurityContext에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
