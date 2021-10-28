package fanap.dinner.auth;

import fanap.dinner.domain.vo.auth.PodToken;
import fanap.dinner.domain.vo.auth.PodTokenType;
import fanap.dinner.domain.vo.exception.ApiErrorVO;
import fanap.dinner.domain.vo.response.ResFact;
import fanap.dinner.domain.vo.response.Result;
import fanap.dinner.exception.Subject;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import fanap.dinner.exception.LogUtils;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.ws.rs.QueryParam;

@CrossOrigin("*")
@RestController
@RequestMapping("/auth")
@Api(tags = {"Auth"})
@SwaggerDefinition(tags = {
        @Tag(name = "Auth", description = "Authentication APIs")
})
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final AuthManager authManager;

    @GetMapping(value = "/podsso")
    @ApiOperation(
            value = "Get Pod SSO address for signing in user.",
            httpMethod = "GET"
    )
    @ApiResponses({
            @ApiResponse(code = 405, message = "method not allowed!", response = ApiErrorVO.class),
            @ApiResponse(code = 500, message = "internal server error!", response = ApiErrorVO.class)
    })
    public void getLoginAddressWithPodSSO(HttpServletResponse response) {
        authManager.getLoginAddressWithPodSSO(response);
    }

    @GetMapping(value = "/podsso_redirect/")
    @ApiOperation(
            value = "Get access and refresh token from Pod SSO and redirect user to panel auth address.",
            httpMethod = "GET"
    )
    @ApiResponses({
            @ApiResponse(code = 400, message = "bad request!", response = ApiErrorVO.class),
            @ApiResponse(code = 405, message = "method not allowed!", response = ApiErrorVO.class)
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "code", value = "code from Pod SSO.", required = true, dataType = "String", paramType = "query", dataTypeClass = String.class)
    })
    public void authorizePODSSO(@QueryParam(value = "code") @NotNull @NotEmpty String code,
                                HttpServletResponse response) {
        try {
            authManager.authorizePODSSO(code, response);
        } catch (Exception e) {
            LogUtils.error(log, e, "Error at authorizePODSSO", Subject.AUTH);
        }
    }

    @GetMapping(value = "/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get new access token by refresh token.", httpMethod = "GET")
    @ApiResponses({
            @ApiResponse(code = 400, message = "bad request!", response = ApiErrorVO.class),
            @ApiResponse(code = 405, message = "method not allowed!", response = ApiErrorVO.class),
            @ApiResponse(code = 406, message = "token is not valid!", response = ApiErrorVO.class),
            @ApiResponse(code = 500, message = "internal server error!", response = ApiErrorVO.class)
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "refreshToken", value = "refresh token", required = true, dataType = "String", paramType = "query", dataTypeClass = String.class)
    })
    public ResponseEntity<Result<PodToken>> getNewTokenByRefreshToken(@RequestParam @NotNull @NotEmpty String refreshToken) {
        return ResponseEntity.ok(ResFact.<PodToken>build()
                .setResult(authManager.getNewAccessToken(refreshToken))
                .get());
    }

    @DeleteMapping(value = "/revoke", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Revoke access token or refresh token.", httpMethod = "DELETE")
    @ApiResponses({
            @ApiResponse(code = 400, message = "bad request!", response = ApiErrorVO.class),
            @ApiResponse(code = 405, message = "method not allowed!", response = ApiErrorVO.class),
            @ApiResponse(code = 500, message = "internal server error!", response = ApiErrorVO.class)
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tokenType", value = "Token type.", required = true, dataType = "PodTokenType", dataTypeClass = PodTokenType.class, paramType = "query"),
            @ApiImplicitParam(name = "token", value = "Access token or Refresh token", required = true, dataType = "String", paramType = "query", dataTypeClass = String.class)
    })
    public ResponseEntity<Result<Boolean>> revokeUserToken(@RequestParam(value = "tokenType") @NotNull PodTokenType tokenType,
                                                           @RequestParam(value = "token") @NotNull @NotEmpty String token) {
        boolean result = authManager.revokeUserToken(tokenType, token);

        return ResponseEntity.ok(ResFact.<Boolean>build()
                .setResult(result)
                .setError(!result)
                .get());
    }

}
