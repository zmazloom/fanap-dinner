package fanap.dinner.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import fanap.dinner.domain.model.group.DinnerGroup;
import fanap.dinner.domain.service.user.PodUserMetadata;
import fanap.dinner.domain.vo.auth.PodToken;
import fanap.dinner.exception.InternalServerException;
import fanap.dinner.exception.LogUtils;
import fanap.dinner.exception.ResourceNotFoundException;
import fanap.dinner.exception.Subject;
import fanap.dinner.message.AuthMessage;
import fanap.dinner.pod.sso.PodSso;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthManagerImpl implements AuthManager {

    private final PodSso podSso;

    @Value("${platform.schema}")
    public String platformSchema;
    @Value("${platform.core.host}")
    public String platformCoreHost;
    @Value("${platform.dashboard}")
    public String platformDashboard;
    @Value("${platform.account.ssoId}")
    public long platformPodSsoId;

    @Override
    public void getLoginAddressWithPodSSO(HttpServletResponse response) {
        String url = podSso.getLoginAddressWithPodSSO();
        try {
            response.sendRedirect(url);
        } catch (IOException e) {
            LogUtils.error(log, e, "Error at getLoginAddressWithPodSSO", Subject.AUTH);
            throw InternalServerException.getInstance(AuthMessage.internalErrorAtGetLoginAddressWithPodSSO());
        }
    }

    @Override
    public void authorizePODSSO(String code, HttpServletResponse response) throws IOException {
        try {
            JSONObject result = podSso.getUserToken(null, code);

            if (result != null) {
                long expiresIn = result.getLong("expires_in");
                String refreshToken = result.getString("refresh_token");
                String accessToken = result.getString("access_token");

                JSONObject userInfo = podSso.getUserFromPOD(accessToken);

                if (userInfo != null) {
                    long userSsoId = userInfo.getLong("ssoId");

                    if (podSso.isUserInGroup(userSsoId, DinnerGroup.FANAP_DINNER_USER.toString())) {
                        response.sendRedirect(getPanelRedirectLoginAddress(false, accessToken, refreshToken, expiresIn, isPodUserActive(userInfo)));
                    } else {
                        //user is not active or first login:
                        if (podSso.addUserToPodUserGroup(userSsoId, DinnerGroup.FANAP_DINNER_USER.toString(), true)) {
                            if (userSsoId == platformPodSsoId)    //active platform pod user when login.
                                changeUserActivate(userSsoId, true);
                            response.sendRedirect(getPanelRedirectLoginAddress(false, accessToken, refreshToken, expiresIn, false));
                        }
                    }

                } else {
                    LogUtils.error(log, fanap.dinner.message.CommonMessage.serverIsNotAccessible("SSO POD"), fanap.dinner.exception.Subject.AUTH);
                    response.sendRedirect(getPanelRedirectLoginAddress(true, accessToken, refreshToken, expiresIn, false));
                }
            }
        } catch (Exception ex) {
            LogUtils.error(log, ex, "Error at authorizePODSSO", fanap.dinner.exception.Subject.AUTH);
            response.sendRedirect(getPanelRedirectLoginAddress(true, null, null, 0, false));
        }
    }

    private String getPanelRedirectLoginAddress(boolean hasError, String accessToken, String refreshToken, long expiresIn, boolean isUserActive) {
        if (hasError)
            return platformSchema + platformDashboard + "." + platformCoreHost + "/err/";

        if (!isUserActive)
            return platformSchema + platformDashboard + "." + platformCoreHost + "/auth" + "?isUserActive=false";

        return platformSchema + platformDashboard + "." + platformCoreHost + "/auth" +
                "?accessToken=" + accessToken +
                "&refreshToken=" + refreshToken +
                "&expiresIn=" + expiresIn +
                "&isUserActive=true";
    }

    @Override
    public PodToken getNewAccessToken(String refreshToken) {
        JSONObject podToken = podSso.getUserToken(refreshToken, null);
        if (podToken == null)
            throw InternalServerException.getInstance(AuthMessage.errorAtGettingAccessToken());

        return PodToken.builder()
                .accessToken(podToken.getString("access_token"))
                .refreshToken(podToken.getString("refresh_token"))
                .scope(podToken.getString("scope"))
                .expiresIn(podToken.getLong("expires_in"))
                .build();
    }

    @Override
    public boolean revokeUserToken(fanap.dinner.domain.vo.auth.PodTokenType tokenType, String token) {
        return podSso.revokeUserToken(tokenType, token);
    }

    @Override
    public boolean isPodUserActive(JSONObject userInfo) {
        try {
            JSONObject clientMetadata = new JSONObject(userInfo.get("client_metadata").toString());
            PodUserMetadata podUserMetadata = new ObjectMapper().readValue(clientMetadata.toString(), fanap.dinner.domain.service.user.PodUserMetadata.class);
            return podUserMetadata.isUserActivation();
        } catch (Exception ex) {
            fanap.dinner.exception.LogUtils.error(log, ex, "Error at isPodUserActive", fanap.dinner.exception.Subject.AUTH);
        }

        return false;
    }

    @Override
    public boolean changeUserActivate(long userSsoId, boolean beActive) {
        if (!podSso.isUserInGroup(userSsoId, DinnerGroup.FANAP_DINNER_USER.toString()))
            throw ResourceNotFoundException.getInstance(fanap.dinner.message.UserMessage.userNotLoginYet(userSsoId));

        PodUserMetadata podUserMetadata = podSso.getUserMetadataFromPod(userSsoId);

        podUserMetadata.setUserActivation(beActive);

        return podSso.updatePodUserMetadata(userSsoId, podUserMetadata);
    }

}
