package rebound.backend.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rebound.backend.member.dtos.requests.MemberModifyRequest;
import rebound.backend.member.dtos.responses.MyInfoResponse;
import rebound.backend.member.service.MemberService;
import rebound.backend.utils.InteractionAuth;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/members/my")
@RequiredArgsConstructor
public class MyPageController {

    private final MemberService memberService;

    @Operation(summary = "마이페이지 정보", description = "회원의 닉네임, 나이, 직군, 프로필 이미지를 반환합니다. 헤더에 토큰 필요")
    @GetMapping
    public ResponseEntity<MyInfoResponse> myPage() {
        Long memberId = InteractionAuth.currentMemberId();
        MyInfoResponse response = memberService.memberInfo(memberId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "마이페이지 정보 수정", description = "회원 닉네임, 나이, 직군, 프로필 이미지를 수정합니다. 헤더에 토큰 필요")
    @PatchMapping
    public ResponseEntity<Void> modifyMyInfo(
            @Valid @RequestPart(value = "request", required = true) MemberModifyRequest request,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        Long memberId = InteractionAuth.currentMemberId();
        try {
            memberService.memberInfoModify(request, imageFile, memberId);
        } catch (IOException e) {
            throw new IllegalArgumentException("이미지 파일 오류");
        }
        return ResponseEntity.noContent().build();
    }
}
