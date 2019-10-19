package cn.webserver.config;

import lombok.Data;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@ConfigurationProperties(prefix = "http")
@Data
@Component
public class HttpClientConfig {
    private int maxTotal;
    private int defaultMaxPerRoute;
    private int connectTimeout;
    private int connectionRequestTimeout;
    private int socketTimeout;
    private boolean staleConnectionCheckEnabled;

    //HttpClient连接池
    @Bean
    public HttpClientConnectionManager httpClientConnectionManager() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(maxTotal);
        connectionManager.setDefaultMaxPerRoute(defaultMaxPerRoute);
        return connectionManager;
    }

    //注册RequestConfig
    @Bean
    public RequestConfig requestConfig() {
        return RequestConfig.custom().setConnectTimeout(connectTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout).setSocketTimeout(socketTimeout)
                .build();
    }

    //注册HttpClient
    @Bean
    public CloseableHttpClient httpClient(HttpClientConnectionManager manager, RequestConfig config) {
        return HttpClientBuilder.create().setConnectionManager(manager).setDefaultRequestConfig(config)
                .build();
    }
}
