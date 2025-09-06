package rebound.backend.ai.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor // Added for JSON deserialization
public class AIRequestDto {
    private String prompt;
    private String title;
    private String failureContent;
}