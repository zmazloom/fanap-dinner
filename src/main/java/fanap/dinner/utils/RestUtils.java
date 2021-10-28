package fanap.dinner.utils;

import fanap.dinner.domain.vo.response.Response;
import fanap.dinner.exception.InternalServerException;
import fanap.dinner.exception.LogUtils;
import fanap.dinner.message.ErrorMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static fanap.dinner.exception.Subject.SEND_REQUEST;

@Slf4j
public class RestUtils {

    private RestUtils() {
    }

    public static Response sendRequest(String baseUrl, String endpoint, HttpMethod method, HttpEntity body,
                                       Map<String, String> headers, MultiValueMap<String, String> params) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl + endpoint);
        if (!CollectionUtils.isEmpty(params))
            builder.queryParams(params);
        String uri = builder.toUriString();

        HttpUriRequest http;
        if (HttpMethod.GET.equals(method)) {
            http = new HttpGet(uri);
        } else if (HttpMethod.POST.equals(method)) {
            http = new HttpPost(uri);

            if (body != null)
                ((HttpPost) http).setEntity(new StringEntity(ModelUtils.toString(body), StandardCharsets.UTF_8));

        } else if (HttpMethod.PUT.equals(method)) {
            http = new HttpPut(uri);

            if (body != null)
                ((HttpPut) http).setEntity(new StringEntity(ModelUtils.toString(body), StandardCharsets.UTF_8));

        } else if (HttpMethod.DELETE.equals(method)) {
            http = new HttpDelete(uri);
        } else {
            throw InternalServerException.getInstance(ErrorMessage.errorInternalServer());
        }

        if (!CollectionUtils.isEmpty(headers)) {
            headers.forEach(http::addHeader);
        }

        try (CloseableHttpClient httpclient = HttpClients.createDefault();
             CloseableHttpResponse response = httpclient.execute(http)) {

            Response res = Response.builder()
                    .status(HttpStatus.valueOf(response.getStatusLine().getStatusCode()))
                    .reason(response.getStatusLine().getReasonPhrase())
                    .build();


            if (response.getEntity() != null)
                res.setContent(EntityUtils.toString(response.getEntity()));

            Map<String, String> headersMap = new HashMap<>();
            if (response.getAllHeaders() != null) {
                for (Header h : response.getAllHeaders())
                    headersMap.put(h.getName(), h.getValue());
            }
            res.setHeaders(headersMap);

            return res;

        } catch (Exception e) {
            LogUtils.error(log, e, e.getMessage(), SEND_REQUEST);
            throw InternalServerException.getInstance(ErrorMessage.errorInternalServer());
        }
    }
}
