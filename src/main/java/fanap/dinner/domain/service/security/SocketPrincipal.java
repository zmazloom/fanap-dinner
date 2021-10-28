package fanap.dinner.domain.service.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.Principal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocketPrincipal implements Principal {

    private String name;
    private String activity;
    private String token;

}