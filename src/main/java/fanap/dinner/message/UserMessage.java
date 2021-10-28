package fanap.dinner.message;

public class UserMessage {

    private UserMessage() {

    }

    public static String userAccountIsInactive() {
        return Translator.toLocale("user.is.inactive", null);
    }

    public static String userActivated() {
        return Translator.toLocale("user.active", null);
    }

    public static String errorAtActivateUser() {
        return Translator.toLocale("error.user.active", null);
    }

    public static String userDeactivated() {
        return Translator.toLocale("user.inactive", null);
    }

    public static String errorAtDeactivateUser() {
        return Translator.toLocale("error.user.inactive", null);
    }

    public static String userGroupCreated() {
        return Translator.toLocale("user.group.create", null);
    }

    public static String userGroupUpdated() {
        return Translator.toLocale("user.group.update", null);
    }

    public static String userAddedToUserGroup() {
        return Translator.toLocale("user.add.to.group", null);
    }

    public static String userRemovedFromUserGroup() {
        return Translator.toLocale("user.removed.from.group", null);
    }

    public static String userNotFound() {
        return Translator.toLocale("user.not.found", null);
    }

    public static String userGroupNotFound() {
        return Translator.toLocale("user.group.not.found", null);
    }

    public static String userNotLoginYet(long userSsoId) {
        return Translator.toLocale("user.not.login.yet", new Object[]{String.valueOf(userSsoId)});
    }

    public static String userIsNotThisGroup(String groupPath) {
        return Translator.toLocale("user.is.not.in.group", new Object[]{groupPath});
    }

    public static String accessDeniedForGroup() {
        return Translator.toLocale("user.group.access.denied", null);
    }

    public static String accessDeniedForAddingToGroup(String groupPath) {
        return Translator.toLocale("user.group.add.access.denied", new Object[]{groupPath});
    }

    public static String accessDeniedForCreateGroup(String groupPath) {
        return Translator.toLocale("user.group.create.access.denied", new Object[]{groupPath});
    }

    public static String userGroupNameIsDuplicate() {
        return Translator.toLocale("user.group.name.duplicate", null);
    }

    public static String invalidGroupName() {
        return Translator.toLocale("user.group.name.invalid", null);
    }

    public static String userGroupRemoved() {
        return Translator.toLocale("user.group.remove", null);
    }

    public static String internalErrorAtCreateGroup() {
        return Translator.toLocale("internal.error.create.group", null);
    }

    public static String internalErrorAtUpdateGroup() {
        return Translator.toLocale("internal.error.update.group", null);
    }

    public static String userIsNotOwnerOfGroup() {
        return Translator.toLocale("user.group.user.is.not.owner", null);
    }

    public static String canNotDeleteOwnerFromUserGroup() {
        return Translator.toLocale("user.group.access.denied.remove.owner", null);
    }
}
