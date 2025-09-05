package rebound.backend.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rebound.backend.member.dtos.requests.MemberModifyRequest;
import rebound.backend.member.dtos.responses.MyInfoResponse;
import rebound.backend.member.service.MemberService;

@RestController
@RequestMapping("/api/v1/members/my")
@RequiredArgsConstructor
public class MyPageController {

    private final MemberService memberService;

    @Operation(summary = "마이페이지 정보", description = "회원의 닉네임, 나이, 직군을 반환합니다. 헤더에 토큰 필요")
    @GetMapping
    public ResponseEntity<MyInfoResponse> myPage(@RequestHeader("Authorization") String token) {
        MyInfoResponse response = memberService.memberInfo(token);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "마이페이지 정보 수정", description = "회원 닉네임, 나이, 직군을 수정합니다. 헤더에 토큰 필요")
    @PatchMapping
    public ResponseEntity<Void> modifyMyInfo(@RequestHeader("Authorization") String token,
                                             @Valid @RequestBody MemberModifyRequest request)
    {
        memberService.memberInfoModify(request, token);
        return ResponseEntity.noContent().build();
    }
}
