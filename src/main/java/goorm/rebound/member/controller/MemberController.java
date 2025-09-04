package goorm.rebound.member.controller;

import goorm.rebound.member.domain.Member;
import goorm.rebound.member.dtos.JoinRequest;
import goorm.rebound.member.dtos.LoginRequest;
import goorm.rebound.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/join")
    public ResponseEntity<String> join(@Valid @RequestBody JoinRequest request) {
        Member member = memberService.join(request);
        return ResponseEntity.ok("회원가입이 완료되었습니다 회원 아이디: " + member.getId());
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) {
        String token = memberService.login(request);
        return ResponseEntity.ok(token);
    }

    //인증 테스트용 API
    //login으로 받은 토큰을 헤더에 Key: Authorization, Value: Bearer <토큰 값>
//    @GetMapping("/test")
//    public ResponseEntity<String> test() {
//        return ResponseEntity.ok("인증 성공!");
//    }

}
