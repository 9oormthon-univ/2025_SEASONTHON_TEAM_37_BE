package rebound.backend.member.dtos.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import rebound.backend.member.domain.Interest;

import java.util.List;

@Getter
public class MemberModifyRequest {
    @NotEmpty
    private String nickname;
    @Min(0)
    private int age;
    @NotEmpty
    private String field;

    private List<Interest> interests;
}
