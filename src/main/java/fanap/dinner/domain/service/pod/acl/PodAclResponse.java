package fanap.dinner.domain.service.pod.acl;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PodAclResponse {
    @Accessors(fluent = true)
    private boolean hasError;
    private String message;
}
