package goorm.rebound.member.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinRequest {
    private String loginId;
    private String password;
    private String nickname;
    private int age;
    private String field;
}
