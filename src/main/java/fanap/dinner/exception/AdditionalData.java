package fanap.dinner.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public abstract class AdditionalData {
    private String key;
    private String value;

    @Override
    public String toString() {
        return key + " : " + value;
    }

}
