package rebound.backend.ai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rebound.backend.ai.dto.AIRequestDto;
import rebound.backend.ai.dto.AIResponseDto;
import rebound.backend.ai.service.GeminiService;

@RestController
@RequestMapping("/api/v1/gemini")
@RequiredArgsConstructor
public class GeminiController {

    private final GeminiService geminiService;

    @PostMapping("/generate-draft")
    public AIResponseDto generateDraft(@RequestBody AIRequestDto requestDto) {
        String prompt = "Give me a draft for a failure story with the title: \"" + requestDto.getTitle() + "\" and the key content: \"" + requestDto.getFailureContent() + "\"";
        String generatedContent = geminiService.generateContent(prompt);
        return new AIResponseDto(generatedContent);
    }
}