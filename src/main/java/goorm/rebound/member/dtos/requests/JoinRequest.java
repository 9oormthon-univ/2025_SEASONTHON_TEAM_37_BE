package goorm.rebound.member.dtos.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinRequest {
    @NotEmpty
    private String loginId;
    @NotEmpty
    private String password;
    @NotEmpty
    private String nickname;
    @Min(0)
    private int age;
    @NotEmpty
    private String field;
}
