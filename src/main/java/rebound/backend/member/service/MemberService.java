package rebound.backend.member.service;

import rebound.backend.member.domain.Member;
import rebound.backend.member.dtos.requests.JoinRequest;
import rebound.backend.member.dtos.requests.MemberModifyRequest;
import rebound.backend.member.dtos.responses.JoinResponse;
import rebound.backend.member.dtos.requests.LoginRequest;
import rebound.backend.member.dtos.responses.LoginResponse;
import rebound.backend.member.dtos.responses.MyInfoResponse;
import rebound.backend.member.repository.MemberRepository;
import rebound.backend.member.util.JwtUtil;
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

    public JoinResponse join(JoinRequest joinRequest) {
        //loginId 중복 검사
        if (memberRepository.findByLoginId(joinRequest.getLoginId()).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 아이디 입니다");
        }

        //nickname 중복 검사
        if (memberRepository.findByNickname(joinRequest.getNickname()).isPresent()) {
            throw new IllegalArgumentException("이미 사용중인 닉네임 입니다");
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

        Member savedMember = memberRepository.save(member);

        return new JoinResponse(savedMember.getLoginId(), savedMember.getId());
    }

    public LoginResponse login(LoginRequest loginRequest) {
        //loginId로 조회
        Member member = memberRepository.findByLoginId(loginRequest.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 아이디 입니다"));

        //비밀번호 일치 확인
        if (!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword_hash())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }

        String token = jwtUtil.createToken(member.getId());

        return new LoginResponse(member.getLoginId(), token);
    }

    public MyInfoResponse memberInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 id 의 회원이 존재하지 않습니다"));

        return new MyInfoResponse(member.getNickname(), member.getAge(), member.getField());
    }

    public void memberInfoModify(MemberModifyRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 id 의 회원이 존재하지 않습니다"));

        Member editMember = Member.builder()
                .id(member.getId())
                .nickname(request.getNickname())
                .age(request.getAge())
                .field(request.getField())
                .loginId(member.getLoginId())
                .password_hash(member.getPassword_hash())
                .createdAt(member.getCreatedAt())
                .build();

        memberRepository.save(editMember);
    }
}
