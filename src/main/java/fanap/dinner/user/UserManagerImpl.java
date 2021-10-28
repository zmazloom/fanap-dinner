package fanap.dinner.user;

import fanap.dinner.auth.AuthManager;
import fanap.dinner.domain.model.group.DinnerGroup;
import fanap.dinner.domain.service.user.PodUserMetadata;
import fanap.dinner.domain.vo.user.UserGroupVO;
import fanap.dinner.domain.vo.user.UserVO;
import fanap.dinner.exception.*;
import fanap.dinner.message.AuthMessage;
import fanap.dinner.message.CommonMessage;
import fanap.dinner.message.UserMessage;
import fanap.dinner.pod.sso.PodSso;
import fanap.dinner.utils.ModelUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagerImpl implements UserManager {

    private final PodSso podSso;
    private final AuthManager authManager;

    @Value("${platform.account.ssoId}")
    public long platformSsoId;
    @Value("${pod.acl.resource.name.regex}")
    public String podAclNamesRegex;
    @Value("${validation.field.length.min.default}")
    public int fieldLengthMinDefault;
    @Value("${validation.field.length.max.default}")
    public int fieldLengthMaxDefault;
    @Value("${validation.description.length.min.default}")
    public int descriptionLengthMinDefault;
    @Value("${validation.description.length.max.default}")
    public int descriptionLengthMaxDefault;


    @Override
    public boolean addUserToGroup(long requesterSsoId, String username, String groupPath) {
        checkUserAccessToGroup(requesterSsoId, groupPath, true, true);

        UserVO userVO = getUserAccountInfoByUsername(username);

        if (userVO.getIsActive().equals(Boolean.FALSE))
            throw NotAcceptableRequestException.getInstance(AuthMessage.userIsNotActive());

        return podSso.addUserToPodUserGroup(userVO.getSsoId(), groupPath, false);
    }

    @Override
    public boolean removeUserFromUserGroup(long requesterSsoId, String username, String groupPath) {
        UserVO userVO = getUserAccountInfoByUsername(username);

        checkUserAccessToGroup(requesterSsoId, groupPath, requesterSsoId != userVO.getSsoId(), true);

        if (checkUserIsOwnerGroup(userVO.getSsoId(), groupPath))
            throw NotAcceptableRequestException.getInstance(UserMessage.canNotDeleteOwnerFromUserGroup());

        return podSso.removeUserFromPodUserGroup(userVO.getSsoId(), groupPath);
    }

    @Override
    public boolean removeUserGroup(long requesterSsoId, String groupPath) {
        checkUserAccessToGroup(requesterSsoId, groupPath, true, false);

        boolean result = podSso.removeUserGroup(groupPath);

        //specify user group owner
        PodUserMetadata podUserMetadata = podSso.getUserMetadataFromPod(requesterSsoId);
        if (podUserMetadata.getUserGroups() != null) {
            podUserMetadata.getUserGroups().remove(groupPath);
            podSso.updatePodUserMetadata(requesterSsoId, podUserMetadata);
        }

        return result;
    }

    @Override
    public JSONArray getAllGroupsOfUser(Long userSsoId) {
        if (userSsoId != null)
            return podSso.getAllGroupsOfUser(userSsoId);

        return new JSONArray();
    }

    @Override
    public UserGroupVO.UserGroupGetVO createUserGroup(UserGroupVO.UserGroupCreateVO userGroupCreateVO, long requesterSsoId) {
        validateUserGroupCreateVO(requesterSsoId, userGroupCreateVO);

        if (getPodUserGroup(userGroupCreateVO.getName()) != null)
            throw ResourceConflictException.getInstance(UserMessage.userGroupNameIsDuplicate());

        if (!podSso.createPodGroup(userGroupCreateVO.getName(), userGroupCreateVO.getTitle(), userGroupCreateVO.getDescription(), requesterSsoId))
            throw InternalServerException.getInstance(UserMessage.internalErrorAtCreateGroup());

        //specify user group owner
        fanap.dinner.domain.service.user.PodUserMetadata podUserMetadata = podSso.getUserMetadataFromPod(requesterSsoId);
        if (podUserMetadata.getUserGroups() == null)
            podUserMetadata.setUserGroups(new ArrayList<>());
        podUserMetadata.getUserGroups().add(userGroupCreateVO.getName());
        podSso.updatePodUserMetadata(requesterSsoId, podUserMetadata);

        return UserGroupVO.UserGroupGetVO.from(userGroupCreateVO);
    }

    @Override
    public UserGroupVO.UserGroupGetVO updateUserGroup(long requesterSsoId, String groupPath, UserGroupVO.UserGroupUpdateVO userGroupUpdateVO) {
        UserGroupVO.UserGroupGetVO userGroupVO = checkUserAccessToGroup(requesterSsoId, groupPath, true, false);

        String oldGroupName = userGroupVO.getName();

        validateUserGroupUpdateVO(requesterSsoId, userGroupVO, userGroupUpdateVO);

        if (userGroupUpdateVO.getName() != null)
            userGroupVO.setName(userGroupUpdateVO.getName());
        if (userGroupUpdateVO.getDescription() != null)
            userGroupVO.setDescription(userGroupUpdateVO.getDescription());
        if (userGroupUpdateVO.getTitle() != null)
            userGroupVO.setTitle(userGroupUpdateVO.getTitle());

        if (!podSso.updatePodGroup(groupPath, userGroupUpdateVO.getName(), userGroupUpdateVO.getTitle(), userGroupUpdateVO.getDescription()))
            throw InternalServerException.getInstance(UserMessage.internalErrorAtUpdateGroup());

        //specify user group owner
        if (ModelUtils.isNotEmpty(userGroupUpdateVO.getName())) {
            fanap.dinner.domain.service.user.PodUserMetadata podUserMetadata = podSso.getUserMetadataFromPod(requesterSsoId);
            if (podUserMetadata.getUserGroups() == null)
                podUserMetadata.setUserGroups(new ArrayList<>());
            podUserMetadata.getUserGroups().remove(oldGroupName);
            podUserMetadata.getUserGroups().add(userGroupUpdateVO.getName());
            podSso.updatePodUserMetadata(requesterSsoId, podUserMetadata);
        }

        return userGroupVO;
    }

    private void validateUserGroupUpdateVO(long requesterSsoId, UserGroupVO userGroupVO, UserGroupVO.UserGroupUpdateVO userGroupUpdateVO) {
        if (userGroupUpdateVO.getName() != null) {
            if (userGroupUpdateVO.getName().isEmpty())
                throw InvalidRequestException.getInstance(CommonMessage.paramRequired("name"));

            if (!userGroupUpdateVO.getName().matches(podAclNamesRegex))
                throw InvalidRequestException.getInstance(UserMessage.invalidGroupName());

            if (!userGroupUpdateVO.getName().startsWith(DinnerGroup.FANAP_DINNER_.toString()))
                userGroupUpdateVO.setName(DinnerGroup.FANAP_DINNER_ + userGroupUpdateVO.getName().toUpperCase());

            if (EnumUtils.isValidEnum(DinnerGroup.class, userGroupUpdateVO.getName().toUpperCase()) && !podSso.isUserInGroup(requesterSsoId, DinnerGroup.FANAP_DINNER_ADMIN.toString()))
                throw AccessDeniedException.getInstance(UserMessage.accessDeniedForAddingToGroup(userGroupUpdateVO.getName()));

            if (!userGroupVO.getName().equalsIgnoreCase(userGroupUpdateVO.getName()) && getPodUserGroup(userGroupUpdateVO.getName()) != null)
                throw ResourceConflictException.getInstance(UserMessage.userGroupNameIsDuplicate());
        }

        if (userGroupUpdateVO.getDescription() != null && userGroupUpdateVO.getDescription().isEmpty())
            userGroupUpdateVO.setDescription(ModelUtils.isNotEmpty(userGroupUpdateVO.getName()) ? userGroupUpdateVO.getName() : userGroupVO.getName());

        if (userGroupUpdateVO.getTitle() != null && userGroupUpdateVO.getTitle().isEmpty())
            userGroupUpdateVO.setTitle(ModelUtils.isNotEmpty(userGroupUpdateVO.getName()) ? userGroupUpdateVO.getName() : userGroupVO.getName());

        GeneralValidator.checkLength(userGroupUpdateVO.getName(), "name", fieldLengthMaxDefault, fieldLengthMinDefault);
        GeneralValidator.checkLength(userGroupUpdateVO.getDescription(), "description", descriptionLengthMaxDefault, descriptionLengthMinDefault);
        GeneralValidator.checkLength(userGroupUpdateVO.getTitle(), "title", fieldLengthMaxDefault, fieldLengthMinDefault);
    }

    private void validateUserGroupCreateVO(long requesterSsoId, UserGroupVO.UserGroupCreateVO userGroupCreateVO) {
        if (!userGroupCreateVO.getName().matches(podAclNamesRegex))
            throw InvalidRequestException.getInstance(UserMessage.invalidGroupName());

        userGroupCreateVO.setName(userGroupCreateVO.getName().toUpperCase());

        if (!userGroupCreateVO.getName().startsWith(DinnerGroup.FANAP_DINNER_.toString()))
            userGroupCreateVO.setName(DinnerGroup.FANAP_DINNER_ + userGroupCreateVO.getName());

        if (requesterSsoId != platformSsoId && EnumUtils.isValidEnum(DinnerGroup.class, userGroupCreateVO.getName()))
            throw AccessDeniedException.getInstance(UserMessage.accessDeniedForCreateGroup(userGroupCreateVO.getName()));

        if (ModelUtils.isEmpty(userGroupCreateVO.getTitle()))
            userGroupCreateVO.setTitle(userGroupCreateVO.getName());

        if (ModelUtils.isEmpty(userGroupCreateVO.getDescription()))
            userGroupCreateVO.setDescription(userGroupCreateVO.getName());

        GeneralValidator.checkLength(userGroupCreateVO.getName(), "name", fieldLengthMaxDefault, fieldLengthMinDefault);
        GeneralValidator.checkLength(userGroupCreateVO.getDescription(), "description", descriptionLengthMaxDefault, descriptionLengthMinDefault);
        GeneralValidator.checkLength(userGroupCreateVO.getTitle(), "title", fieldLengthMaxDefault, fieldLengthMinDefault);
    }

    public UserGroupVO.UserGroupGetVO getPodUserGroup(String groupPath) {
        JSONObject group = podSso.getUserGroup(groupPath.toUpperCase());

        if (group != null)
            return convertPodGroupToUserGroupGetVO(group);

        return null;
    }

    public List<UserGroupVO.UserGroupGetVO> getAllUserGroupsOfUserFromPod(long requesterSsoId) {
        List<UserGroupVO.UserGroupGetVO> userGroupGetVOS = new ArrayList<>();

        JSONArray groups = podSso.getAllGroupsOfUser(requesterSsoId);

        if (groups != null) {
            for (Object groupObject : groups) {
                JSONObject group = new JSONObject(groupObject.toString());
                if (!group.getString("name").toUpperCase().startsWith(DinnerGroup.FANAP_DINNER_.toString()))
                    continue;

                userGroupGetVOS.add(convertPodGroupToUserGroupGetVO(group));
            }
        }

        return userGroupGetVOS;
    }

    @Override
    public List<UserGroupVO.UserGroupGetVO> getAllUserGroups(long requesterSsoId) {
        List<UserGroupVO.UserGroupGetVO> userGroupGetVOS;

        if (userHaveFullAccessToAllThings(requesterSsoId))
            userGroupGetVOS = getAllGroups();
        else
            userGroupGetVOS = getAllUserGroupsOfUserFromPod(requesterSsoId);

        //specify owner of groups
        PodUserMetadata podUserMetadata = podSso.getUserMetadataFromPod(requesterSsoId);
        if (podUserMetadata.getUserGroups() != null) {

            for (UserGroupVO.UserGroupGetVO userGroupVO : userGroupGetVOS) {
                if (podUserMetadata.getUserGroups().contains(userGroupVO.getName()))
                    userGroupVO.setOwner(true);
            }

        }

        return userGroupGetVOS;
    }

    public List<UserGroupVO.UserGroupGetVO> getAllGroups() {
        List<UserGroupVO.UserGroupGetVO> userGroupGetVOS = new ArrayList<>();

        JSONArray groups = podSso.getAllUserGroups();

        if (groups != null) {
            for (Object groupObject : groups) {
                JSONObject group = new JSONObject(groupObject.toString());
                if (!group.getString("name").toUpperCase().startsWith(DinnerGroup.FANAP_DINNER_.toString()))
                    continue;

                userGroupGetVOS.add(convertPodGroupToUserGroupGetVO(group));
            }
        }

        return userGroupGetVOS;
    }

    @Override
    public List<UserVO> getGroupUsers(Long requesterSsoId, String groupPath) {
        if (requesterSsoId != null) {
            checkUserAccessToGroup(requesterSsoId, groupPath, false, false);
            if (!userHaveFullAccessToAllThings(requesterSsoId) && !podSso.isUserInGroup(requesterSsoId, groupPath))
                throw AccessDeniedException.getInstance(UserMessage.userIsNotThisGroup(groupPath));
        }

        List<UserVO> userVOS = new ArrayList<>();

        JSONObject groupInfo = podSso.getUserGroup(groupPath);

        if (groupInfo != null) {
            JSONArray members = new JSONArray(groupInfo.get("members").toString());

            for (Object memberObject : members) {
                JSONObject member = new JSONObject(memberObject.toString());

                userVOS.add(convertPodProfileUserToUserVO(member, false));
            }
        }

        return userVOS;
    }

    @Override
    public UserVO getUserAccountInfo(String accessToken) {
        JSONObject userInfo = podSso.getUserFromPOD(accessToken);
        return convertPodUserToUserVO(userInfo);
    }

    private UserVO convertPodUserToUserVO(JSONObject userInfo) {
        if (userInfo != null)
            return UserVO.builder()
                    .ssoId(userInfo.getLong("ssoId"))
                    .firstName(userInfo.has("firstName") ? userInfo.getString("firstName") : null)
                    .lastName(userInfo.has("lastName") ? userInfo.getString("lastName") : null)
                    .username(userInfo.getString("username"))
                    .email(userInfo.getString("email"))
                    .avatar((userInfo.has("profileImage") ? userInfo.getString("profileImage") : null))
                    .isActive(authManager.isPodUserActive(userInfo))
                    .build();

        return null;
    }

    private UserVO convertPodProfileUserToUserVO(JSONObject userInfo, boolean checkActivation) {
        if (userInfo != null)
            return UserVO.builder()
                    .ssoId(userInfo.getLong("id"))
                    .firstName(userInfo.has("given_name") ? userInfo.getString("given_name") : null)
                    .lastName(userInfo.has("family_name") ? userInfo.getString("family_name") : null)
                    .username(userInfo.has("preferred_username") ? userInfo.getString("preferred_username") : null)
                    .email(userInfo.has("email") ? userInfo.getString("email") : null)
                    .avatar(userInfo.has("picture") ? userInfo.getString("picture") : null)
                    .isActive(!checkActivation ? null : authManager.isPodUserActive(userInfo))
                    .build();

        return null;
    }

    @Override
    public UserVO getUserAccountInfoBySsoId(long userSsoId) {
        if (!podSso.isUserInGroup(userSsoId, DinnerGroup.FANAP_DINNER_USER.toString()))
            throw ResourceNotFoundException.getInstance(UserMessage.userNotLoginYet(userSsoId));

        JSONObject userInfo = podSso.getUserFromPOD(null, userSsoId);

        return convertPodProfileUserToUserVO(userInfo, true);
    }

    public boolean checkUserIsActive(long userSsoId) {
        JSONObject userInfo = podSso.getUserFromPOD(null, userSsoId);
        UserVO userVO = convertPodProfileUserToUserVO(userInfo, true);
        return userVO != null && userVO.getIsActive() != null && userVO.getIsActive();
    }

    @Override
    public List<UserVO> searchUserAccountInfoByUsername(String username) {
        List<UserVO> allUsers = getGroupUsers(null, DinnerGroup.FANAP_DINNER_USER.toString());

        for (int i = 0; i < allUsers.size(); i++) {
            if (!allUsers.get(i).getUsername().toUpperCase().contains(username.toUpperCase())) {
                allUsers.remove(i);
                i--;
            }
        }

        return allUsers;
    }

    /**
     * Receives user account information in PodSso based on 'username'.
     *
     * @param username the username of user.
     * @return the information of user in PodSso.
     * @throws ResourceNotFoundException If user not found in platform users.
     */
    @NotNull
    public UserVO getUserAccountInfoByUsername(String username) {
        JSONObject userInfo = podSso.getUserFromPOD(username, null);

        if (userInfo != null) {
            long userSsoId = userInfo.getLong("id");

            if (podSso.isUserInGroup(userSsoId, DinnerGroup.FANAP_DINNER_USER.toString())) {
                UserVO userVO = convertPodProfileUserToUserVO(userInfo, true);
                if (Boolean.TRUE.equals(userVO.getIsActive()))
                    return userVO;
            }
        }

        throw ResourceNotFoundException.getInstance(UserMessage.userNotFound());
    }

    private UserGroupVO.UserGroupGetVO checkUserAccessToGroup(long requesterSsoId, String groupPath, boolean userMustBeGroupOwner, boolean checkFullAccess) {
        UserGroupVO.UserGroupGetVO userGroup = getPodUserGroup(groupPath);
        if (userGroup == null)
            throw ResourceNotFoundException.getInstance(UserMessage.userGroupNotFound());

        if (checkFullAccess && userHaveFullAccessToAllThings(requesterSsoId))
            return userGroup;

        if (userMustBeGroupOwner && !checkUserIsOwnerGroup(requesterSsoId, groupPath))
            throw AccessDeniedException.getInstance(UserMessage.userIsNotOwnerOfGroup());

        return userGroup;
    }

    private boolean checkUserIsOwnerGroup(long requesterSsoId, String groupPath) {
        PodUserMetadata podUserMetadata = podSso.getUserMetadataFromPod(requesterSsoId);

        return podUserMetadata.getUserGroups() != null && podUserMetadata.getUserGroups().contains(groupPath);
    }

    private UserGroupVO.UserGroupGetVO convertPodGroupToUserGroupGetVO(JSONObject group) {
        return UserGroupVO.UserGroupGetVO.builder()
                .name(group.getString("name"))
                .title(group.getString("title"))
                .description(group.getString("description"))
                .path(group.getString("path"))
                .build();
    }

    @Override
    public void removeUserFromAllGroups(long userSsoId) {
        try {
            JSONArray groups = podSso.getAllGroupsOfUser(userSsoId);

            if (groups != null) {
                for (Object groupObject : groups) {
                    JSONObject group = new JSONObject(groupObject.toString());
                    String groupName = group.getString("name");
                    if (EnumUtils.isValidEnum(DinnerGroup.class, groupName) ||
                            !group.getString("name").toUpperCase().startsWith(DinnerGroup.FANAP_DINNER_.toString()))
                        continue;

                    podSso.removeUserFromPodUserGroup(userSsoId, groupName);
                }
            }
        } catch (Exception ex) {
            LogUtils.error(log, ex, "Error at removeUserFromAllGroups", Subject.USER_GROUP);
        }
    }

    private boolean userHaveFullAccessToAllThings(long requesterSsoId) {
        return requesterSsoId == platformSsoId || podSso.isUserInGroup(requesterSsoId, DinnerGroup.FANAP_DINNER_ADMIN.toString());
    }

}
