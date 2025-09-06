import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AIRequestDto {
    private String prompt;
    // 다른 입력 필드의 내용이 있다면 여기에 추가 가능
    private String title;
    private String failureContent;
}