package fanap.dinner.domain.vo.response;

import fanap.dinner.message.CommonMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.net.HttpURLConnection;

@Data
@Builder
@AllArgsConstructor
public class Result<T> {
    @Builder.Default
    private int code = HttpURLConnection.HTTP_OK;
    @Builder.Default
    private String message = CommonMessage.ok();
    private T result;
    private boolean error;
    private long total;

    public Result() {
    }

    public Result(T result) {
        this.result = result;
    }

    public Result(int code, String message, T result) {
        this.code = code;
        this.message = message;
        this.result = result;
    }

    public Result(int code, String message, T result, boolean error) {
        this.code = code;
        this.message = message;
        this.result = result;
    }
}
