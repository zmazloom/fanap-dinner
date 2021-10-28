package fanap.dinner.user;

import fanap.dinner.domain.vo.user.UserGroupVO;
import fanap.dinner.domain.vo.user.UserVO;
import org.json.JSONArray;

import java.util.List;

public interface UserManager {

    /**
     * Add user to user group. Just group owner can add member to group.
     *
     * @param requesterSsoId Sso id of user that want to add member.
     * @param username       Username of new member.
     * @param groupPath      Group path in pod.
     */
    boolean addUserToGroup(long requesterSsoId, String username, String groupPath);

    boolean removeUserFromUserGroup(long requesterSsoId, String username, String groupPath);

    boolean removeUserGroup(long requesterSsoId, String groupPath);

    /**
     * Create user group in pod.
     *
     * @param userGroupCreateVO Information of new group.
     * @param requesterSsoId    Sso id of user that want to create group.
     */
    UserGroupVO.UserGroupGetVO createUserGroup(UserGroupVO.UserGroupCreateVO userGroupCreateVO, long requesterSsoId);

    /**
     * Update user group information. Just group owner can update group.
     *
     * @param requesterSsoId    Sso id of user that want to add member.
     * @param groupPath         Group path in pod.
     * @param userGroupUpdateVO New information of group.
     */
    UserGroupVO.UserGroupGetVO updateUserGroup(long requesterSsoId, String groupPath, UserGroupVO.UserGroupUpdateVO userGroupUpdateVO);

    /**
     * Get all user groups. For platform account and admin user returns all groups.
     *
     * @param requesterSsoId Sso id of user.
     */
    List<UserGroupVO.UserGroupGetVO> getAllUserGroups(long requesterSsoId);

    List<UserVO> getGroupUsers(Long requesterSsoId, String groupPath);

    /**
     * Search username in all users and return all users that have this username characters.
     *
     * @param username Some characters of username
     */
    List<UserVO> searchUserAccountInfoByUsername(String username);

    UserVO getUserAccountInfoBySsoId(long userSsoId);

    UserVO getUserAccountInfo(String accessToken);

    void removeUserFromAllGroups(long userSsoId);

    JSONArray getAllGroupsOfUser(Long userSsoId);

}
