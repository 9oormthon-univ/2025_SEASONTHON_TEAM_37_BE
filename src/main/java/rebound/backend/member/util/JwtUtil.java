package rebound.backend.member.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key secretKey;
    private final long expirationHours;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationHours = 24; //유효기간 24시간
    }

    //로그인 ID 를 받아 JWT 토큰 생성
    public String createToken(Long memberId) {
        Date now= new Date();
        Date expirationDate = new Date(now.getTime() + expirationHours * 60 * 60 * 1000);

        return Jwts.builder()
                .setSubject(memberId.toString())
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    //토큰에서 로그인 ID 추출
    public String getLoginIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    // ✅ [수정] 사용자 정보를 함께 검증하는 validateToken 메서드
    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String loginIdFromToken = getLoginIdFromToken(token);
            // 1. 토큰의 사용자 정보와 DB에서 가져온 사용자 정보가 일치하는지 확인
            // 2. 토큰이 만료되었는지 확인
            return loginIdFromToken.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            // 그 외 모든 예외는 유효하지 않은 토큰으로 처리
            return false;
        }
    }

    // 토큰의 만료 여부만 확인하는 private 메서드
    private boolean isTokenExpired(String token) {
        final Date expiration = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expiration.before(new Date());
    }

//    public boolean validateToken(String token) {
//        try {
//            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
}
