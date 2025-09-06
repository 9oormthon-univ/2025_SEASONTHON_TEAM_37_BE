package rebound.backend.member.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import rebound.backend.category.entity.MainCategory;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class MyInfoResponse {
    private String nickname;
    private int age;
    private String field;
    private String imageUrl;
    private List<MainCategory> interests;
}
