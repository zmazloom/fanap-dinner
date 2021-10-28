package fanap.dinner.domain.vo.user;

import fanap.dinner.utils.ModelUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;


@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class UserGroupVO {

    @NotNull
    @NotEmpty
    @ApiModelProperty(position = 1, value = "User group name. Group name must be unique and starts with FANAP_DINNER_. max len = 255", required = true)
    private String name;
    @ApiModelProperty(position = 2, value = "User group title. If empty, the group name is set. max len = 255")
    private String title;
    @ApiModelProperty(position = 3, value = "User group description. If empty, the group name is set. max len = 1500")
    private String description;

    @ApiModel("UserGroupCreateVO")
    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    public static class UserGroupCreateVO extends UserGroupVO {
    }

    @ApiModel("UserGroupUpdateVO")
    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    public static class UserGroupUpdateVO extends UserGroupVO {

    }

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    public static class UserGroupGetVO extends UserGroupVO {
        @ApiModelProperty(value = "User Group path")
        private String path;

        @ApiModelProperty(value = "If true, user is owner for this group.")
        private boolean isOwner;

        public static UserGroupGetVO from(UserGroupCreateVO userGroupCreateVO) {
            if (userGroupCreateVO == null)
                return null;

            UserGroupVO.UserGroupGetVO userGroupVO = ModelUtils.getModelMapper().map(userGroupCreateVO, UserGroupVO.UserGroupGetVO.class);
            userGroupVO.setPath(userGroupCreateVO.getName());
            userGroupVO.setOwner(true);
            return userGroupVO;
        }
    }

}
