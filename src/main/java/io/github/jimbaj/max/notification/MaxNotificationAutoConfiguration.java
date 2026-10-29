package io.github.jimbaj.max.notification;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
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

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    FilterRegistrationBean<MaxWebhookSecretFilter> maxWebhookSecretFilter(MaxNotificationProperties properties) {
        FilterRegistrationBean<MaxWebhookSecretFilter> registration =
                new FilterRegistrationBean<>(new MaxWebhookSecretFilter(properties));
        registration.addUrlPatterns(properties.getWebhook().getPath());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public MaxUpdateHandler maxUpdateHandler() {
        return update -> {};
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public MaxWebhookController maxWebhookController(MaxUpdateHandler handler) {
        return new MaxWebhookController(handler);
    }
}