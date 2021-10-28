package fanap.dinner.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public class AdditionalTag extends AdditionalData{
    public AdditionalTag(String key, String value) {
        super(key, value);
    }

    @Override
    public String toString() {
        return getKey() + " : " + getValue();
    }

}
