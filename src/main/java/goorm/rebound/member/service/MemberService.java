package goorm.rebound.member.service;

import goorm.rebound.member.domain.Member;
import goorm.rebound.member.dtos.JoinRequest;
import goorm.rebound.member.dtos.LoginRequest;
import goorm.rebound.member.repository.MemberRepository;
import goorm.rebound.member.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public Member join(JoinRequest joinRequest) {
        //loginId 중복 검사
        if (memberRepository.findByLoginId(joinRequest.getLoginId()).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 아이디 입니다");
        }

        //비밀번호 해싱
        String hashedPassword = passwordEncoder.encode(joinRequest.getPassword());

        Member member = Member.builder()
                .loginId(joinRequest.getLoginId())
                .password_hash(hashedPassword)
                .nickname(joinRequest.getNickname())
                .age(joinRequest.getAge())
                .field(joinRequest.getField())
                .createdAt(LocalDateTime.now())
                .build();

        return memberRepository.save(member);
    }

    public String login(LoginRequest loginRequest) {
        //loginId로 조회
        Member member = memberRepository.findByLoginId(loginRequest.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 아이디 입니다"));

        //비밀번호 일치 확인
        if (!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword_hash())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        return jwtUtil.createToken(member.getLoginId());
    }
}
