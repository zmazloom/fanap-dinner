package fanap.dinner.security;

import fanap.dinner.utils.ModelUtils;
import fanap.dinner.user.UserManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import fanap.dinner.domain.service.security.SocketPrincipal;
import fanap.dinner.domain.vo.user.UserVO;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

@Slf4j
public class AuthorizationFilter extends BasicAuthenticationFilter {

    private final UserManager userService;

    private static final String TOKEN_BEARER_TYPE = "Bearer";

    public AuthorizationFilter(AuthenticationManager authenticationManager,
                               UserManager userService) {
        super(authenticationManager);
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith(TOKEN_BEARER_TYPE)) {
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (token != null) {
            Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();

            try {
                token = token.replace(TOKEN_BEARER_TYPE + " ", "");
                if (ModelUtils.isEmpty(token))
                    return null;

                SocketPrincipal socketPrincipal = null;
                UserVO userVO = userService.getUserAccountInfo(token);
                //check activity
                if (userVO != null) {
                    JSONArray groups = userService.getAllGroupsOfUser(userVO.getSsoId());

                    for (Object group : groups) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + ((JSONObject) group).get("path").toString()));
                    }

                    socketPrincipal = SocketPrincipal.builder()
                            .name(userVO.getSsoId() != null ? userVO.getSsoId().toString() : null)
                            .token(token)
                            .activity(userVO.getIsActive().toString())
                            .build();
                }
                return new UsernamePasswordAuthenticationToken(socketPrincipal, token, authorities);

            } catch (Exception e) {
                //nothing
            }
        }

        return null;
    }

}
