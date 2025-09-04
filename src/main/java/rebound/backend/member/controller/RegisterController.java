package rebound.backend.member.controller;

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

    @PostMapping("/join")
    public ResponseEntity<JoinResponse> join(@Valid @RequestBody JoinRequest request) {
        JoinResponse response = memberService.join(request);
        return ResponseEntity.ok(response);
    }

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
