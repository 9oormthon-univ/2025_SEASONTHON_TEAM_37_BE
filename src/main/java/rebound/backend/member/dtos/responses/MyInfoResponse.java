package rebound.backend.member.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MyInfoResponse {
    private String nickname;
    private int age;
    private String field;
}
