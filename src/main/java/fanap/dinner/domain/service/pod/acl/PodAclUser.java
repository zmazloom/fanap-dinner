package fanap.dinner.domain.service.pod.acl;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PodAclUser {
    private Long id;

    @JsonProperty("preferred_username")
    private String preferredUsername;

    @JsonProperty("given_name")
    private String givenName;

    @JsonProperty("family_name")
    private String familyName;

    private String picture;

    @JsonProperty("phone_number_verified")
    private Boolean phoneNumberVerified;

    @JsonProperty("email_verified")
    private Boolean emailVerified;

    @JsonProperty("nationalcode_verified")
    private Boolean nationalCodeVerified;
}
