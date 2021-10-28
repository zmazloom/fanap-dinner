package fanap.dinner.user;

import fanap.dinner.auth.AuthUtils;
import fanap.dinner.domain.vo.exception.ApiErrorVO;
import fanap.dinner.domain.vo.response.ResFact;
import fanap.dinner.domain.vo.response.Result;
import fanap.dinner.domain.vo.user.UserGroupVO;
import fanap.dinner.domain.vo.user.UserVO;
import fanap.dinner.exception.InternalServerException;
import fanap.dinner.message.ErrorMessage;
import fanap.dinner.message.UserMessage;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/group")
@Api(tags = {"User Group"})
@SwaggerDefinition(tags = {
        @Tag(name = "User Group", description = "User Group APIs")
})
@Slf4j
@RequiredArgsConstructor
public class UserGroupController {

    private final UserManager userService;

    @PostMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('SAKKU_MARKETPLACE_USER', 'SAKKU_MARKETPLACE_SUPERVISOR', 'SAKKU_MARKETPLACE_ADMIN')")
    @ApiOperation(value = "Create user group.", httpMethod = "POST")
    @ApiResponses({
            @ApiResponse(code = 400, message = "bad request!", response = ApiErrorVO.class),
            @ApiResponse(code = 401, message = "not authorized!", response = ApiErrorVO.class),
            @ApiResponse(code = 403, message = "access denied!", response = ApiErrorVO.class),
            @ApiResponse(code = 404, message = "user not found!", response = ApiErrorVO.class),
            @ApiResponse(code = 405, message = "method not allowed!", response = ApiErrorVO.class),
            @ApiResponse(code = 409, message = "group name is duplicate!", response = ApiErrorVO.class),
            @ApiResponse(code = 500, message = "internal server error!", response = ApiErrorVO.class)
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userGroupVO", value = "User group information", required = true, dataType = "UserGroupCreateVO", dataTypeClass = UserGroupVO.UserGroupCreateVO.class, paramType = "body")
    })
    public ResponseEntity<Result<UserGroupVO.UserGroupGetVO>> createUserGroup(@RequestBody @NotNull @Validated UserGroupVO.UserGroupCreateVO userGroupVO) {
        long requesterSsoId = AuthUtils.getCurrentUserSsoId();

        return ResponseEntity.ok(ResFact.<UserGroupVO.UserGroupGetVO>build()
                .setMessage(UserMessage.userGroupCreated())
                .setResult(userService.createUserGroup(userGroupVO, requesterSsoId))
                .get());
    }

    @PutMapping(value = "/{groupPath}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('SAKKU_MARKETPLACE_USER', 'SAKKU_MARKETPLACE_SUPERVISOR', 'SAKKU_MARKETPLACE_ADMIN')")
    @ApiOperation(value = "Update user group.", httpMethod = "PUT")
    @ApiResponses({
            @ApiResponse(code = 400, message = "bad request!", response = ApiErrorVO.class),
            @ApiResponse(code = 401, message = "not authorized!", response = ApiErrorVO.class),
            @ApiResponse(code = 403, message = "access denied!", response = ApiErrorVO.class),
            @ApiResponse(code = 404, message = "group not found!", response = ApiErrorVO.class),
            @ApiResponse(code = 405, message = "method not allowed!", response = ApiErrorVO.class),
            @ApiResponse(code = 409, message = "group name is duplicate!", response = ApiErrorVO.class),
            @ApiResponse(code = 500, message = "internal server error!", response = ApiErrorVO.class)
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userGroupVO", value = "User group information", required = true, dataType = "UserGroupUpdateVO", dataTypeClass = UserGroupVO.UserGroupUpdateVO.class, paramType = "body")
    })
    public ResponseEntity<Result<UserGroupVO.UserGroupGetVO>> updateUserGroup(@PathVariable(value = "groupPath") @NotNull @NotEmpty String groupPath,
                                                                              @RequestBody @NotNull UserGroupVO.UserGroupUpdateVO userGroupVO) {
        long requesterSsoId = AuthUtils.getCurrentUserSsoId();

        return ResponseEntity.ok(ResFact.<UserGroupVO.UserGroupGetVO>build()
                .setMessage(UserMessage.userGroupUpdated())
                .setResult(userService.updateUserGroup(requesterSsoId, groupPath, userGroupVO))
                .get());
    }

    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('SAKKU_MARKETPLACE_USER', 'SAKKU_MARKETPLACE_SUPERVISOR', 'SAKKU_MARKETPLACE_ADMIN')")
    @ApiOperation(value = "Get all user groups for user.", httpMethod = "GET")
    @ApiResponses({
            @ApiResponse(code = 400, message = "bad request!", response = ApiErrorVO.class),
            @ApiResponse(code = 401, message = "access denied!", response = ApiErrorVO.class),
            @ApiResponse(code = 405, message = "method not allowed!", response = ApiErrorVO.class)
    })
    public ResponseEntity<Result<List<UserGroupVO.UserGroupGetVO>>> getAllUserGroupsOfUser() {
        long requesterSsoId = AuthUtils.getCurrentUserSsoId();

        return ResponseEntity.ok(ResFact.<List<UserGroupVO.UserGroupGetVO>>build()
                .setResult(userService.getAllUserGroups(requesterSsoId))
                .get());
    }

    @GetMapping(value = "/{groupPath}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('SAKKU_MARKETPLACE_USER', 'SAKKU_MARKETPLACE_SUPERVISOR', 'SAKKU_MARKETPLACE_ADMIN')")
    @ApiOperation(value = "Get all users in user groups.", httpMethod = "GET")
    @ApiResponses({
            @ApiResponse(code = 400, message = "bad request!", response = ApiErrorVO.class),
            @ApiResponse(code = 403, message = "access denied!", response = ApiErrorVO.class),
            @ApiResponse(code = 404, message = "group not found!", response = ApiErrorVO.class),
            @ApiResponse(code = 405, message = "method not allowed!", response = ApiErrorVO.class),
            @ApiResponse(code = 500, message = "internal server error!", response = ApiErrorVO.class)
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupPath", value = "Pod group path", required = true, dataType = "String", dataTypeClass = String.class, paramType = "path")
    })
    public ResponseEntity<Result<List<UserVO>>> getUsersInUserGroup(@PathVariable(value = "groupPath") @NotNull @NotEmpty String groupPath) {
        long requesterSsoId = AuthUtils.getCurrentUserSsoId();

        return ResponseEntity.ok(ResFact.<List<UserVO>>build()
                .setResult(userService.getGroupUsers(requesterSsoId, groupPath.toUpperCase()))
                .get());
    }

    @PostMapping(value = "/{groupPath}/user/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('SAKKU_MARKETPLACE_USER', 'SAKKU_MARKETPLACE_SUPERVISOR', 'SAKKU_MARKETPLACE_ADMIN')")
    @ApiOperation(value = "Add user to user group.", httpMethod = "POST")
    @ApiResponses({
            @ApiResponse(code = 400, message = "bad request!", response = ApiErrorVO.class),
            @ApiResponse(code = 403, message = "access denied!", response = ApiErrorVO.class),
            @ApiResponse(code = 404, message = "user or group not found!", response = ApiErrorVO.class),
            @ApiResponse(code = 405, message = "method not allowed!", response = ApiErrorVO.class)
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "Username of user", required = true, dataType = "String", dataTypeClass = String.class, paramType = "path", example = "username"),
            @ApiImplicitParam(name = "groupPath", value = "Group path", required = true, dataType = "String", dataTypeClass = String.class, paramType = "path", example = "general")
    })
    public ResponseEntity<Result<Boolean>> addUserToUserGroup(@PathVariable(value = "username") @NotNull String username,
                                                              @PathVariable(value = "groupPath") @NotNull @NotEmpty String groupPath) {
        long requesterSsoId = AuthUtils.getCurrentUserSsoId();

        boolean result = userService.addUserToGroup(requesterSsoId, username, groupPath.toUpperCase());

        return ResponseEntity.ok(ResFact.<Boolean>build()
                .setMessage(result ? UserMessage.userAddedToUserGroup() : ErrorMessage.errorInternalServer())
                .setResult(result)
                .setError(!result)
                .get());
    }

    @DeleteMapping(value = "/{groupPath}/user/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('FANAP_DINNER_USER', 'FANAP_DINNER_ADMIN')")
    @ApiOperation(value = "Remove user from user group.", httpMethod = "DELETE")
    @ApiResponses({
            @ApiResponse(code = 400, message = "bad request!", response = ApiErrorVO.class),
            @ApiResponse(code = 403, message = "access denied!", response = ApiErrorVO.class),
            @ApiResponse(code = 404, message = "user or group not found!", response = ApiErrorVO.class),
            @ApiResponse(code = 405, message = "method not allowed!", response = ApiErrorVO.class)
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "Username of user", required = true, dataType = "String", dataTypeClass = String.class, paramType = "path", example = "username"),
            @ApiImplicitParam(name = "groupPath", value = "Group path", required = true, dataType = "String", dataTypeClass = String.class, paramType = "path", example = "general")
    })
    public ResponseEntity<Result<Boolean>> removeUserFromUserGroup(@PathVariable(value = "username") @NotNull String username,
                                                                   @PathVariable(value = "groupPath") @NotNull @NotEmpty String groupPath) {
        long requesterSsoId = AuthUtils.getCurrentUserSsoId();

        boolean result = userService.removeUserFromUserGroup(requesterSsoId, username, groupPath.toUpperCase());

        if (!result)
            throw InternalServerException.getInstance(ErrorMessage.errorInternalServer());

        return ResponseEntity.ok(ResFact.<Boolean>build()
                .setMessage(UserMessage.userRemovedFromUserGroup())
                .setResult(true)
                .get());
    }

    @DeleteMapping(value = "/{groupPath}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('SAKKU_MARKETPLACE_USER', 'SAKKU_MARKETPLACE_SUPERVISOR', 'SAKKU_MARKETPLACE_ADMIN')")
    @ApiOperation(value = "Remove user group.", httpMethod = "DELETE")
    @ApiResponses({
            @ApiResponse(code = 400, message = "bad request!", response = ApiErrorVO.class),
            @ApiResponse(code = 403, message = "access denied!", response = ApiErrorVO.class),
            @ApiResponse(code = 404, message = "group not found!", response = ApiErrorVO.class),
            @ApiResponse(code = 405, message = "method not allowed!", response = ApiErrorVO.class)
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupPath", value = "Group path", required = true, dataType = "String", dataTypeClass = String.class, paramType = "path", example = "general")
    })
    public ResponseEntity<Result<Boolean>> removeGroup(@PathVariable(value = "groupPath") @NotNull @NotEmpty String groupPath) {
        long requesterSsoId = AuthUtils.getCurrentUserSsoId();

        boolean result = userService.removeUserGroup(requesterSsoId, groupPath.toUpperCase());

        if (!result)
            throw InternalServerException.getInstance(ErrorMessage.errorInternalServer());

        return ResponseEntity.ok(ResFact.<Boolean>build()
                .setMessage(UserMessage.userGroupRemoved())
                .setResult(true)
                .get());
    }

}
