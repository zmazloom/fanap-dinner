package fanap.dinner.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public class AdditionalExtra extends AdditionalData {

    public AdditionalExtra(String key, String value) {
        super(key, value);
    }

}
