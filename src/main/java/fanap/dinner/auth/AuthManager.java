package fanap.dinner.auth;


import fanap.dinner.domain.vo.auth.PodToken;
import org.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface AuthManager {

    void getLoginAddressWithPodSSO(HttpServletResponse response);

    void authorizePODSSO(String code, HttpServletResponse response) throws IOException;

    PodToken getNewAccessToken(String refreshToken);

    boolean revokeUserToken(fanap.dinner.domain.vo.auth.PodTokenType tokenType, String token);

    /**
     * Active or deactivate user. It changes userActivation parameter in client metadata of pod user.
     *
     * @param userSsoId pod sso id of user
     * @param beActive  if true, then activate user.
     */
    boolean changeUserActivate(long userSsoId, boolean beActive);

    /**
     * Check user is active or not. Get client metadata of pod user info and check userActivation parameter is true or not.
     *
     * @param userInfo output of get user from pod api.
     */
    boolean isPodUserActive(JSONObject userInfo);

}
