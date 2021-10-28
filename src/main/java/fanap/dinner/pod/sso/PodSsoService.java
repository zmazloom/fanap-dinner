package fanap.dinner.pod.sso;

import fanap.dinner.message.CommonConstants;
import fanap.dinner.utils.ModelUtils;
import fanap.dinner.domain.service.user.PodUserMetadata;
import fanap.dinner.domain.vo.auth.PodTokenType;
import fanap.dinner.exception.*;
import fanap.dinner.utils.ServiceUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import fanap.dinner.message.AuthMessage;
import fanap.dinner.message.Translator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
@Component
public class PodSsoService implements PodSso {

    @Value("${podsso.url.authorize}")
    public String podSsoUrlAuthorize;
    @Value("${podsso.client.id}")
    public String podSsoClientId;
    @Value("${podsso.client.secret}")
    private String podSsoClientSecret;
    @Value("${podsso.responsetype}")
    public String podSsoResponseType;
    @Value("${podsso.url.token}")
    private String podSsoTokenUrl;
    @Value("${platform.login.address}")
    private String platformLoginAddress;
    @Value("${pod.api.platform.address}")
    private String platformAddress;
    @Value("${podsso.url}")
    private String podSsoUrl;
    @Value("${podsso.group.url}")
    private String podSsoGroupBaseUrl;
    @Value("${platform.podsso.scope}")
    private String platformPodSsoScope;

    @Override
    public String getLoginAddressWithPodSSO() {
        return podSsoUrlAuthorize + "?client_id=" + podSsoClientId +
                "&response_type=" + podSsoResponseType +
                "&redirect_uri=" + getPODSSORedirectUri() +
                "&scope=" + platformPodSsoScope;
    }

    @Override
    public JSONObject getUserToken(String refreshToken, String code) {
        try {
            HttpPost request = new HttpPost(podSsoTokenUrl);

            List<NameValuePair> params = new ArrayList<>(5);
            if (ModelUtils.isNotEmpty(refreshToken)) {
                params.add(new BasicNameValuePair("grant_type", "refresh_token"));
                params.add(new BasicNameValuePair("refresh_token", refreshToken));
            } else {
                params.add(new BasicNameValuePair("grant_type", "authorization_code"));
                params.add(new BasicNameValuePair("code", code));
            }

            params.add(new BasicNameValuePair("redirect_uri", getPODSSORedirectUri()));
            params.add(new BasicNameValuePair("client_id", podSsoClientId));
            params.add(new BasicNameValuePair("client_secret", podSsoClientSecret));

            request.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
            request.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

            HttpResponse response = ServiceUtils.executeCertificateClient(request);

            if (response.getEntity() != null && response.getStatusLine().getStatusCode() == 200) {
                String rawResponse = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                return new JSONObject(rawResponse);
            } else if (response.getStatusLine().getStatusCode() == 400 || response.getStatusLine().getStatusCode() == 500) {
                throw NotAcceptableRequestException.getInstance(AuthMessage.tokenIsExpired());
            } else {
                LogUtils.error(log, response, Subject.AUTH);
            }
        } catch (ProjectRuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            LogUtils.error(log, ex, "Error at getUserTokenByRefreshToken", Subject.AUTH);
        }

        return null;
    }

    @Override
    public JSONObject getUserFromPOD(String accessToken) {
        try {
            HttpGet request = new HttpGet(platformAddress + "/nzh/getUserProfile" +
                    "?client_id=" + podSsoClientId +
                    "&client_secret=" + podSsoClientSecret);

            request.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
            setHeaderForSsoServices(request, accessToken);

            HttpResponse response = ServiceUtils.executeCertificateClient(request);

            if (response.getStatusLine().getStatusCode() == 200) {
                String responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                JSONObject data = new JSONObject(responseString);
                if (data.get("hasError").toString().equals("false")) {
                    return data.getJSONObject("result");
                } else {
                    LogUtils.error(log, "GetUserFromPOD : Error executing http: " + responseString, Subject.USER);
                }
            } else {
                LogUtils.error(log, response, Subject.USER);
            }
        } catch (IOException e) {
            LogUtils.fatal(log, e, "Error at GetUserFromPOD", Subject.USER);
        }

        return null;
    }

    @Override
    public JSONObject getUserFromPOD(String username, Long userId) {
        try {
            String endpoint;
            if (userId != null)
                endpoint = "/" + userId + "?identityType=id";
            else
                endpoint = "/" + username + "?identityType=username";

            HttpGet request = new HttpGet(podSsoUrl + "/users" + endpoint);
            setBasicAuthHeaderForSsoService(request);

            HttpResponse response = ServiceUtils.executeCertificateClient(request);

            if (response.getStatusLine().getStatusCode() == 200) {
                String responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                return new JSONObject(responseString);
            }
        } catch (IOException e) {
            LogUtils.fatal(log, e, "Error at GetUserFromPOD", Subject.USER);
        }

        return null;
    }

    @Override
    public boolean revokeUserToken(PodTokenType tokenType, String token) {
        try {
            HttpPost request = new HttpPost(podSsoUrl + "/oauth2/token/revoke?token=" + token +
                    "&token_type_hint=" + tokenType.toString().toLowerCase());

            setBasicAuthHeaderForSsoService(request);

            HttpResponse response = ServiceUtils.executeCertificateClient(request);

            if (response.getEntity() != null && response.getStatusLine().getStatusCode() == 200) {
                return true;
            }
        } catch (Exception ex) {
            LogUtils.error(log, ex, "Error at revokeUserToken", Subject.AUTH);
        }

        return false;
    }

    @Override
    public boolean updatePodUserMetadata(long userSsoId, PodUserMetadata podUserMetadata) {
        try {
            HttpPost request = new HttpPost(podSsoUrl + "/users/" + userSsoId + "?identityType=id");

            List<NameValuePair> params = new ArrayList<>(2);
            params.add(new BasicNameValuePair("identityType", "id"));
            params.add(new BasicNameValuePair("client_metadata", new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(podUserMetadata)));

            setBasicAuthHeaderForSsoService(request);
            request.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

            HttpResponse response = ServiceUtils.executeCertificateClient(request);

            if (response.getEntity() != null && response.getStatusLine().getStatusCode() == 200) {
                return true;
            } else {
                LogUtils.error(log, response, Subject.USER);
            }
        } catch (Exception ex) {
            LogUtils.error(log, ex, "Error at updatePodUserMetadata", Subject.USER);
        }

        return false;
    }

    @Override
    public boolean addUserToPodUserGroup(long userSsoId, String groupPath, boolean createGroupIfNotExist) {
        //create group if not exist
        if (createGroupIfNotExist && !createPodGroup(groupPath, groupPath, groupPath, userSsoId))
            return false;

        try {
            HttpPut request = new HttpPut(podSsoGroupBaseUrl + "/" + groupPath + "/members?member=" + userSsoId + "&memberType=id");

            setBasicAuthHeaderForSsoService(request);

            HttpResponse response = ServiceUtils.executeCertificateClient(request);

            if (response.getEntity() != null && response.getStatusLine().getStatusCode() == 200)
                return true;
            else if (response.getStatusLine().getStatusCode() == 400) {
                JSONObject data = new JSONObject(EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
                throw InvalidRequestException.getInstance(data.get("error_description").toString());
            }
        } catch (IOException ex) {
            LogUtils.error(log, ex, "Error at addUserToGroup", Subject.USER_GROUP);
        }

        return false;
    }

    @Override
    public boolean isUserInGroup(long userSsoId, String groupPath) {
        try {
            JSONArray groups = getAllGroupsOfUser(userSsoId);
            for (Object group : groups) {
                if (group != null && ((JSONObject) group).get("path").toString().equalsIgnoreCase(groupPath))
                    return true;
            }
        } catch (Exception ex) {
            LogUtils.error(log, ex, "Error at isUserInGroup", Subject.USER_GROUP);
        }

        return false;
    }

    @Override
    public boolean createPodGroup(String name, String title, String description, long userSsoId) {
        try {
            HttpPut request = new HttpPut(podSsoGroupBaseUrl);

            List<NameValuePair> params = new ArrayList<>(6);
            params.add(new BasicNameValuePair("name", name));
            params.add(new BasicNameValuePair("description", description));
            params.add(new BasicNameValuePair("title", title));
            params.add(new BasicNameValuePair("cascadeGroupAccess", "true"));
            params.add(new BasicNameValuePair("member", String.valueOf(userSsoId)));
            params.add(new BasicNameValuePair("memberType", "id"));

            setBasicAuthHeaderForSsoService(request);
            request.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

            HttpResponse response = ServiceUtils.executeCertificateClient(request);

            if (response.getEntity() != null &&
                    (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 409)) {
                return true;
            } else {
                LogUtils.error(log, response, Subject.USER_GROUP);
            }
        } catch (Exception ex) {
            LogUtils.error(log, ex, "Error at createPodGroup", Subject.USER_GROUP);
        }

        return false;
    }

    @Override
    public boolean updatePodGroup(String groupPath, String name, String title, String description) {
        try {
            HttpPost request = new HttpPost(podSsoGroupBaseUrl + "/" + groupPath);

            List<NameValuePair> params = new ArrayList<>(4);
            params.add(new BasicNameValuePair("name", name));
            params.add(new BasicNameValuePair("description", description));
            params.add(new BasicNameValuePair("title", title));
            params.add(new BasicNameValuePair("cascadeGroupAccess", "true"));

            setBasicAuthHeaderForSsoService(request);
            request.setEntity(new UrlEncodedFormEntity(params, StandardCharsets.UTF_8));

            HttpResponse response = ServiceUtils.executeCertificateClient(request);

            if (response.getEntity() != null && response.getStatusLine().getStatusCode() == 200) {
                return true;
            } else {
                LogUtils.error(log, response, Subject.USER_GROUP);
            }
        } catch (Exception ex) {
            LogUtils.error(log, ex, "Error at updatePodGroup", Subject.USER_GROUP);
        }

        return false;
    }

    @Override
    public JSONArray getAllGroupsOfUser(long userSsoId) {
        try {
            HttpGet request = new HttpGet(podSsoGroupBaseUrl + "/users/" + userSsoId + "/groups?identityType=id&offset=0&size=1000");

            setBasicAuthHeaderForSsoService(request);

            HttpResponse response = ServiceUtils.executeCertificateClient(request);

            if (response.getEntity() != null && response.getStatusLine().getStatusCode() == 200) {
                String responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                JSONObject data = new JSONObject(responseString);
                return new JSONArray(data.get("groups").toString());
            }
        } catch (Exception ex) {
            LogUtils.error(log, ex, "Error at getAllGroupsOfUser", Subject.USER_GROUP);
        }

        return new JSONArray();
    }

    @Override
    public JSONArray getAllUserGroups() {
        try {
            HttpGet request = new HttpGet(podSsoGroupBaseUrl);

            setBasicAuthHeaderForSsoService(request);

            HttpResponse response = ServiceUtils.executeCertificateClient(request);

            if (response.getEntity() != null && response.getStatusLine().getStatusCode() == 200) {
                String responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                JSONObject data = new JSONObject(responseString);
                return new JSONArray(data.get("groups").toString());
            }
        } catch (Exception ex) {
            LogUtils.error(log, ex, "Error at getAllUserGroups", Subject.USER_GROUP);
        }

        return new JSONArray();
    }

    @Override
    public JSONObject getUserGroup(String groupPath) {
        try {
            HttpGet request = new HttpGet(podSsoGroupBaseUrl + "/" + groupPath);

            setBasicAuthHeaderForSsoService(request);

            HttpResponse response = ServiceUtils.executeCertificateClient(request);

            if (response.getEntity() != null && response.getStatusLine().getStatusCode() == 200) {
                String responseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                return new JSONObject(responseString);
            }
        } catch (Exception ex) {
            LogUtils.fatal(log, ex, "Error at getAllGroupsOfUser", Subject.USER_GROUP);
        }

        return null;
    }

    @Override
    public boolean removeUserFromPodUserGroup(long userSsoId, String groupPath) {
        try {
            HttpDelete request = new HttpDelete(podSsoGroupBaseUrl + "/" + groupPath + "/members?member=" + userSsoId + "&memberType=id");

            setBasicAuthHeaderForSsoService(request);

            HttpResponse response = ServiceUtils.executeCertificateClient(request);

            if (response.getEntity() != null && response.getStatusLine().getStatusCode() == 200)
                return true;
            else {
                LogUtils.error(log, response, Subject.USER_GROUP);
            }
        } catch (IOException ex) {
            LogUtils.error(log, ex, "Error at removeUserFromPodUserGroup", Subject.USER_GROUP);
        }

        return false;
    }

    @Override
    public boolean removeUserGroup(String groupPath) {
        try {
            HttpDelete request = new HttpDelete(podSsoGroupBaseUrl + "/" + groupPath);

            setBasicAuthHeaderForSsoService(request);

            HttpResponse response = ServiceUtils.executeCertificateClient(request);

            if (response.getEntity() != null && response.getStatusLine().getStatusCode() == 200)
                return true;
            else {
                LogUtils.error(log, response, Subject.USER_GROUP);
            }
        } catch (IOException ex) {
            LogUtils.error(log, ex, "Error at removeUserGroup", Subject.USER_GROUP);
        }

        return false;
    }

    @Override
    public PodUserMetadata getUserMetadataFromPod(long requesterSsoId) {
        try {
            JSONObject userInfo = getUserFromPOD(null, requesterSsoId);
            if (userInfo != null) {
                String userMetadata = userInfo.get("client_metadata").toString();
                return new ObjectMapper().readValue(userMetadata, PodUserMetadata.class);
            }
        } catch (Exception ex) {
            LogUtils.error(log, ex, "Error at getUserMetadataFromPod", Subject.USER);
        }

        return PodUserMetadata.builder().build();
    }

    private String getPODSSORedirectUri() {
        return platformLoginAddress;
    }

    private void setHeaderForSsoServices(HttpUriRequest request, String userAccessToken) {
        request.setHeader(CommonConstants.SERVICE_TOKEN, userAccessToken);
        request.setHeader(CommonConstants.SERVICE_TOKEN_ISSUER, "1");
    }

    private void setBasicAuthHeaderForSsoService(HttpUriRequest request) {
        String encoding = Base64.getEncoder().encodeToString((podSsoClientId + ":" + podSsoClientSecret).getBytes(StandardCharsets.UTF_8));
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
        request.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
        request.setHeader(HttpHeaders.ACCEPT_LANGUAGE, Translator.getRequestLanguage());
    }

}
