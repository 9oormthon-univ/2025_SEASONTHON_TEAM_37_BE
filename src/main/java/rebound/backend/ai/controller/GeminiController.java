import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/gemini")
public class GeminiController {

    private final GeminiService geminiService;

    public GeminiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/generate-draft")
    public AIResponseDto generateDraft(@RequestBody AIRequestDto requestDto) {
        // 1. DTO를 통해 프론트엔드로부터 받은 프롬프트를 서비스로 전달
        String generatedContent = geminiService.generateContent(requestDto.getPrompt());

        // 2. 서비스의 응답을 DTO에 담아 프론트엔드로 반환
        return new AIResponseDto(generatedContent);
    }
}