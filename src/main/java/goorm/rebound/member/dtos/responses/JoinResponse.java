package goorm.rebound.member.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class JoinResponse {
    private String loginId;
    private Long memberId;
}
