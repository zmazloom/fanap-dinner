package fanap.dinner.utils;

import fanap.dinner.exception.Subject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.json.JSONObject;
import fanap.dinner.exception.LogUtils;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
public class ServiceUtils {

    private ServiceUtils() {
    }

    public static String getContentOfHttpResponse(HttpResponse response) throws IOException {
        if (response.getEntity() != null) {
            BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));

            StringBuilder resultBuilder = new StringBuilder();
            String line;
            while ((line = bufferedreader.readLine()) != null) {
                resultBuilder.append(line);
            }
            bufferedreader.close();
            return resultBuilder.toString();
        } else {
            return null;
        }
    }

    public static HttpResponse sendGetRequestAndGetResponse(String url, Map<String, String> headers) throws IOException {
        HttpGet request = new HttpGet(url);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                request.addHeader(entry.getKey(), entry.getValue());
            }
        }

        HttpClient httpClient = HttpClientBuilder.create().build();

        return httpClient.execute(request);
    }

    public static String sendGetRequestAndGetContent(String url, Map<String, String> headers) throws IOException {
        HttpResponse httpResponse = sendGetRequestAndGetResponse(url, headers);
        return getContentOfHttpResponse(httpResponse);
    }

    public static String sendJsonPostRequest(String url, JSONObject payload, Map<String, String> headers) throws IOException {
        HttpPost post = new HttpPost(url);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                post.addHeader(entry.getKey(), entry.getValue());
            }
        }
        post.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        post.setEntity(new StringEntity(String.valueOf(payload), StandardCharsets.UTF_8));
        HttpClient httpClient = HttpClientBuilder.create().build();

        HttpResponse response = httpClient.execute(post);

        if (response.getStatusLine().getStatusCode() != 200) {
            LogUtils.error(log, response, Subject.UNSPECIFIED);
            return null;
        }

        return getContentOfHttpResponse(response);

    }

    public static HttpResponse sendJsonPatchRequest(String url, JSONObject payload, Map<String, String> headers) throws IOException {
        HttpPatch patchRequest = new HttpPatch(url);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                patchRequest.addHeader(entry.getKey(), entry.getValue());
            }
        }
        patchRequest.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        patchRequest.setEntity(new StringEntity(String.valueOf(payload), StandardCharsets.UTF_8));
        HttpClient httpClient = HttpClientBuilder.create().build();

        return httpClient.execute(patchRequest);
    }

    public static HttpResponse sendDeleteRequest(String url, Map<String, String> headers) throws IOException {
        HttpDelete http = new HttpDelete(url);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                http.addHeader(entry.getKey(), entry.getValue());
            }
        }
        http.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        HttpClient httpClient = HttpClientBuilder.create().build();

        return httpClient.execute(http);
    }

    public static HttpResponse executeHttpClient(HttpUriRequest request) throws IOException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        return httpClient.execute(request);
    }

    public static HttpResponse executeCertificateClient(HttpUriRequest request) throws IOException {
        HttpClient httpclient = createAcceptSelfSignedCertificateClient();
        return httpclient.execute(request);
    }

    public static CloseableHttpClient createAcceptSelfSignedCertificateClient() {

        try {
            // use the TrustSelfSignedStrategy to allow Self Signed Certificates
            SSLContext sslContext = SSLContextBuilder
                    .create()
                    .loadTrustMaterial(new TrustSelfSignedStrategy())
                    .build();

            // we can optionally disable hostname verification.
            // if you don't want to further weaken the security, you don't have to include this.
            HostnameVerifier allowAllHosts = new NoopHostnameVerifier();


            // create an SSL Socket Factory to use the SSLContext with the trust self signed certificate strategy
            // and allow all hosts verifier.
            SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, allowAllHosts);

            // finally create the HttpClient using HttpClient factory methods and assign the ssl socket factory
            return HttpClients
                    .custom()
                    .setSSLSocketFactory(connectionFactory)
                    .build();
        } catch (Exception e) {
            return HttpClients.createDefault();
        }
    }

}