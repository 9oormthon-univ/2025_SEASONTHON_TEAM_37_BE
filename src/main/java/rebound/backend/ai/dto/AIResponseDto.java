import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor // 모든 필드를 포함한 생성자를 자동으로 만듭니다.
public class AIResponseDto {
    private String generatedContent;
}