package fanap.dinner.exception.log;

import fanap.dinner.exception.AdditionalTag;
import org.springframework.lang.Nullable;

/**
 * This class integrates all additional data in logs.
 * keys must be in snake_case
 */
public class LogData {

    private LogData() {
    }

    public static AdditionalTag serviceId(@Nullable Long serviceId) {
        return new AdditionalTag("service_id", String.valueOf(serviceId));
    }

}
