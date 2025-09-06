package rebound.backend.member.dtos.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import rebound.backend.category.entity.MainCategory;

import java.util.List;

@Getter
public class MemberModifyRequest {
    @NotEmpty
    private String nickname;
    @Min(0)
    private int age;
    @NotEmpty
    private String field;
    private String imageUrl;

    private List<MainCategory> interests;
}
