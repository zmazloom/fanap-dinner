package fanap.dinner.user;

import fanap.dinner.auth.AuthManager;
import fanap.dinner.auth.AuthUtils;
import fanap.dinner.document.AdminAPI;
import fanap.dinner.domain.vo.exception.ApiErrorVO;
import fanap.dinner.domain.vo.response.ResFact;
import fanap.dinner.domain.vo.response.Result;
import fanap.dinner.domain.vo.user.UserVO;
import fanap.dinner.message.UserMessage;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/user")
@Api(tags = {"User"})
@SwaggerDefinition(tags = {
        @Tag(name = "User", description = "User APIs")
})
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final AuthManager authManager;
    private final UserManager userManager;

    @AdminAPI
    @PutMapping(value = "/{userId}/activate", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('SAKKU_MARKETPLACE_ADMIN')")
    @ApiOperation(value = "Activate user.", httpMethod = "PUT")
    @ApiResponses({
            @ApiResponse(code = 400, message = "bad request!", response = ApiErrorVO.class),
            @ApiResponse(code = 401, message = "access denied!", response = ApiErrorVO.class),
            @ApiResponse(code = 404, message = "user not found!", response = ApiErrorVO.class),
            @ApiResponse(code = 405, message = "method not allowed!", response = ApiErrorVO.class)
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "SSO id of user", required = true, dataType = "Long", paramType = "path", example = "1")
    })
    public ResponseEntity<Result<Boolean>> activateUser(@PathVariable(value = "userId") @NotNull Long userSsoId) {
        AuthUtils.getCurrentUserAccessToken();

        boolean result = authManager.changeUserActivate(userSsoId, true);

        return ResponseEntity.ok(ResFact.<Boolean>build()
                .setMessage(result ? UserMessage.userActivated() : UserMessage.errorAtActivateUser())
                .setResult(result)
                .setError(!result)
                .setTotal(result ? 1 : 0)
                .get());
    }

    @AdminAPI
    @PutMapping(value = "/{userId}/deactivate", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('SAKKU_MARKETPLACE_ADMIN')")
    @ApiOperation(value = "Deactivate user.", httpMethod = "PUT")
    @ApiResponses({
            @ApiResponse(code = 400, message = "bad request!", response = ApiErrorVO.class),
            @ApiResponse(code = 401, message = "access denied!", response = ApiErrorVO.class),
            @ApiResponse(code = 404, message = "user not found!", response = ApiErrorVO.class),
            @ApiResponse(code = 405, message = "method not allowed!", response = ApiErrorVO.class)
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "SSO id of user", required = true, dataType = "Long", paramType = "path", example = "1")
    })
    public ResponseEntity<Result<Boolean>> deactivateUser(@PathVariable(value = "userId") @NotNull Long userSsoId) {
        AuthUtils.getCurrentUserAccessToken();

        boolean result = authManager.changeUserActivate(userSsoId, false);

        userManager.removeUserFromAllGroups(userSsoId);

        return ResponseEntity.ok(ResFact.<Boolean>build()
                .setMessage(result ? UserMessage.userDeactivated() : UserMessage.errorAtDeactivateUser())
                .setResult(result)
                .setError(!result)
                .setTotal(result ? 1 : 0)
                .get());
    }

    @AdminAPI
    @GetMapping(value = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('SAKKU_MARKETPLACE_ADMIN')")
    @ApiOperation(value = "Get user account info by sso id.", httpMethod = "GET")
    @ApiResponses({
            @ApiResponse(code = 400, message = "bad request!", response = ApiErrorVO.class),
            @ApiResponse(code = 401, message = "access denied!", response = ApiErrorVO.class),
            @ApiResponse(code = 404, message = "user not found!", response = ApiErrorVO.class),
            @ApiResponse(code = 405, message = "method not allowed!", response = ApiErrorVO.class)
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "Sso id of user", required = true, dataType = "Long", dataTypeClass = Long.class, paramType = "path", example = "1")
    })
    public ResponseEntity<Result<UserVO>> getUserAccountInfoBySsoId(@PathVariable(value = "userId") @NotNull Long userId) {
        AuthUtils.getCurrentUserSsoId();

        return ResponseEntity.ok(ResFact.<UserVO>build()
                .setResult(userManager.getUserAccountInfoBySsoId(userId))
                .get());
    }

    @GetMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('SAKKU_MARKETPLACE_USER', 'SAKKU_MARKETPLACE_SUPERVISOR', 'SAKKU_MARKETPLACE_DATACENTER_ADMIN', 'SAKKU_MARKETPLACE_ADMIN')")
    @ApiOperation(value = "Get user account information.", httpMethod = "GET")
    @ApiResponses({
            @ApiResponse(code = 400, message = "bad request!", response = ApiErrorVO.class),
            @ApiResponse(code = 401, message = "access denied!", response = ApiErrorVO.class),
            @ApiResponse(code = 405, message = "method not allowed!", response = ApiErrorVO.class)
    })
    public ResponseEntity<Result<UserVO>> getUserAccountInfo() {
        String accessToken = AuthUtils.getCurrentUserAccessToken();

        return ResponseEntity.ok(ResFact.<UserVO>build()
                .setResult(userManager.getUserAccountInfo(accessToken))
                .get());
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('SAKKU_MARKETPLACE_USER', 'SAKKU_MARKETPLACE_SUPERVISOR', 'SAKKU_MARKETPLACE_DATACENTER_ADMIN', 'SAKKU_MARKETPLACE_ADMIN')")
    @ApiOperation(value = "Search user account by username.", httpMethod = "GET")
    @ApiResponses({
            @ApiResponse(code = 400, message = "bad request!", response = ApiErrorVO.class),
            @ApiResponse(code = 401, message = "access denied!", response = ApiErrorVO.class),
            @ApiResponse(code = 405, message = "method not allowed!", response = ApiErrorVO.class)
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "username", required = true, dataType = "String", dataTypeClass = String.class, paramType = "query")
    })
    public ResponseEntity<Result<List<UserVO>>> searchUserAccountInfoByUsername(@RequestParam(value = "username") @NotNull @NotEmpty String username) {
        AuthUtils.getCurrentUserSsoId();

        return ResponseEntity.ok(ResFact.<List<UserVO>>build()
                .setResult(userManager.searchUserAccountInfoByUsername(username))
                .get());
    }

}
