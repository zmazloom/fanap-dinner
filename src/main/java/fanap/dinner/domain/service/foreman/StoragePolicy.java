package fanap.dinner.domain.service.foreman;

import fanap.dinner.domain.model.service.resource.plan.disk.MainDisk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoragePolicy {
    private int iops;
    private MainDisk.Type type;
}
