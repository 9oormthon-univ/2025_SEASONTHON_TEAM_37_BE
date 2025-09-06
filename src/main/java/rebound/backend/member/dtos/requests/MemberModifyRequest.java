package rebound.backend.member.dtos.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

@Getter
public class MemberModifyRequest {
    @NotEmpty
    private String nickname;
    @Min(0)
    private int age;
    @NotEmpty
    private String field;
}
