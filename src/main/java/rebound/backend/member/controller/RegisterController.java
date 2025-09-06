package rebound.backend.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import rebound.backend.member.dtos.requests.JoinRequest;
import rebound.backend.member.dtos.responses.JoinResponse;
import rebound.backend.member.dtos.requests.LoginRequest;
import rebound.backend.member.dtos.responses.LoginResponse;
import rebound.backend.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class RegisterController {

    private final MemberService memberService;

    @Operation(summary = "회원 가입", description = "loginId, 비밀번호, 닉네임, 나이, 직군을 받아 회원가입 / id 나 닉네임 중복시 오류")
    @PostMapping("/join")
    public ResponseEntity<JoinResponse> join(@Valid @RequestBody JoinRequest request) {
        JoinResponse response = memberService.join(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로그인", description = "loginId, 비밀번호로 로그인하면 해당 회원의 id값과 토큰 반환")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = memberService.login(request);
        return ResponseEntity.ok(response);
    }

    //인증 테스트용 API
    //login으로 받은 토큰을 헤더에 Key: Authorization, Value: Bearer <토큰 값>
//    @GetMapping("/test")
//    public ResponseEntity<String> test() {
//        return ResponseEntity.ok("인증 성공!");
//    }

}
