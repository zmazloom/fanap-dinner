package fanap.dinner.domain.service.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Structure of client_metadata of pod user
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PodUserMetadata {

    @Builder.Default
    private boolean userActivation = false;

    @Builder.Default
    private List<String> userGroups = new ArrayList<>();

}
