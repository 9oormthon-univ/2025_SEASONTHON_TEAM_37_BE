package rebound.backend.domain.s3.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rebound.backend.domain.s3.service.S3Service;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class S3Controller {
    private final S3Service s3Service;

    @Operation(summary = "(테스트)사진 S3에 저장하고 url 반환")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String url = s3Service.uploadFile(file);
        return ResponseEntity.ok(url);
    }
}
