package io.github.jimbaj.max.notification;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@AutoConfiguration
@EnableConfigurationProperties(MaxNotificationProperties.class)
@ConditionalOnProperty(prefix = "max.notification", name = "token")
public class MaxNotificationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    MaxNotificationClient maxNotificationClient(MaxNotificationProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.getConnectTimeoutMs()))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(properties.getReadTimeoutMs()));

        RestClient restClient = RestClient.builder()
                .baseUrl(properties.getApiUrl())
                .requestFactory(factory)
                .build();

        return new MaxNotificationClient(restClient, properties.getToken());
    }
}