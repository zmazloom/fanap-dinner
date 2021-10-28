package fanap.dinner.pod.sso;

import fanap.dinner.domain.service.user.PodUserMetadata;
import fanap.dinner.domain.vo.auth.PodTokenType;
import org.json.JSONArray;
import org.json.JSONObject;

public interface PodSso {

    String getLoginAddressWithPodSSO();

    JSONObject getUserToken(String refreshToken, String code);

    JSONObject getUserFromPOD(String accessToken);

    JSONObject getUserFromPOD(String username, Long userId);

    boolean revokeUserToken(PodTokenType tokenType, String token);

    boolean updatePodUserMetadata(long userSsoId, PodUserMetadata podUserMetadata);

    boolean addUserToPodUserGroup(long userSsoId, String groupPath, boolean createGroupIfNotExist);

    boolean removeUserFromPodUserGroup(long userSsoId, String groupPath);

    boolean createPodGroup(String name, String title, String description, long userSsoId);

    boolean updatePodGroup(String groupPath, String name, String title, String description);

    boolean isUserInGroup(long userSsoId, String groupPath);

    JSONArray getAllGroupsOfUser(long userSsoId);

    JSONArray getAllUserGroups();

    JSONObject getUserGroup(String groupPath);

    boolean removeUserGroup(String groupPath);

    PodUserMetadata getUserMetadataFromPod(long requesterSsoId);

}
