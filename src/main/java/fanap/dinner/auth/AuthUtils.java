package fanap.dinner.auth;

import fanap.dinner.domain.service.security.SocketPrincipal;
import fanap.dinner.exception.AccessDeniedException;
import fanap.dinner.message.ErrorMessage;
import fanap.dinner.message.UserMessage;
import fanap.dinner.utils.ModelUtils;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtils {

    private AuthUtils() {
    }

    /**
     * retrieve user from security context
     *
     * @return current User SSO ID
     * @throws AccessDeniedException if user not found or user account is not active
     */
    public static Long getCurrentUserSsoId() {
        SocketPrincipal socketPrincipal = (SocketPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (socketPrincipal == null || ModelUtils.isEmpty(socketPrincipal.getName()))
            throw AccessDeniedException.getInstance(ErrorMessage.accessDenied());

        if (socketPrincipal.getActivity() == null || !socketPrincipal.getActivity().equalsIgnoreCase("true"))
            throw AccessDeniedException.getInstance(UserMessage.userAccountIsInactive());

        return Long.valueOf(socketPrincipal.getName());
    }

    /**
     * retrieve user from security context
     *
     * @return current User Access Token
     * @throws AccessDeniedException if user not found
     */
    public static String getCurrentUserAccessToken() {
        SocketPrincipal socketPrincipal = (SocketPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (socketPrincipal == null || ModelUtils.isEmpty(socketPrincipal.getToken()))
            throw AccessDeniedException.getInstance(ErrorMessage.accessDenied());

        return socketPrincipal.getToken();
    }

}
