package rebound.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 팀 합의: posts/** 는 일단 전부 보호(나중에 메인 목록 열 때 조정)
                        .requestMatchers("/api/v1/posts/**").authenticated()

                        // 댓글 하트/삭제/작성 보호
                        .requestMatchers(HttpMethod.POST,   "/api/v1/comments/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/comments/**").authenticated()

                        // 그 외는 임시로 오픈
                        .anyRequest().permitAll()
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req,res,ex) -> {
                            res.setStatus(401);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"error\":{\"code\":\"AUTH_REQUIRED\",\"message\":\"로그인이 필요합니다.\"}}");
                        })
                );
        return http.build();
    }
}
