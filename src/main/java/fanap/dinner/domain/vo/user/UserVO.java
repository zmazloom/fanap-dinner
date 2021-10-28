package fanap.dinner.domain.vo.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserVO {

    private Long ssoId;
    private String firstName;
    private String lastName;
    private String username;
    private String avatar;
    private String email;
    private Boolean isActive;

}
