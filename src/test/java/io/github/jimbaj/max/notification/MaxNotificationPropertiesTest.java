package io.github.jimbaj.max.notification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class MaxNotificationPropertiesTest {

    /**
     * Тестовая конфигурация: без явного {@code @EnableConfigurationProperties}
     * Spring Boot не забиндит {@link MaxNotificationProperties}, и тест
     * проверял бы пустой объект.
     */
    @Configuration
    @EnableConfigurationProperties(MaxNotificationProperties.class)
    static class TestConfig {
    }

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Test
    void bindsDefaultsWhenNoPropertiesSet() {
        runner.run(context -> {
            MaxNotificationProperties props = context.getBean(MaxNotificationProperties.class);

            assertThat(props.getWebhook()).isNotNull();
            assertThat(props.getWebhook().getPath()).isEqualTo("/max/webhook");
            assertThat(props.getWebhook().getSecret()).isNull();
        });
    }

    @Test
    void bindsCustomWebhookPathAndSecret() {
        runner.withPropertyValues(
                "max.notification.webhook.path=/custom/webhook",
                "max.notification.webhook.secret=s3cret-value"
        ).run(context -> {
            MaxNotificationProperties props = context.getBean(MaxNotificationProperties.class);

            assertThat(props.getWebhook().getPath()).isEqualTo("/custom/webhook");
            assertThat(props.getWebhook().getSecret()).isEqualTo("s3cret-value");
        });
    }

    @Test
    void secretStaysNullWhenPropertyNotSet() {
        // path задан, secret — нет: секрет должен остаться null,
        // это будущий признак «приём webhook выключен».
        runner.withPropertyValues(
                "max.notification.webhook.path=/custom/webhook"
        ).run(context -> {
            MaxNotificationProperties props = context.getBean(MaxNotificationProperties.class);

            assertThat(props.getWebhook().getSecret()).isNull();
        });
    }
}