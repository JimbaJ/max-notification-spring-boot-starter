package io.github.jimbaj.max.notification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

class MaxNotificationAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MaxNotificationAutoConfiguration.class));

    @Test
    void createsClientWhenTokenPresent() {
        runner.withPropertyValues("max.notification.token=test-token")
                .run(context -> assertThat(context).hasSingleBean(MaxNotificationClient.class));
    }

    @Test
    void createsNothingWhenTokenMissing() {
        runner.run(context -> assertThat(context).doesNotHaveBean(MaxNotificationClient.class));
    }

    @Test
    void emptyToken_clientBeanIsAbsent_contextStartsWithoutError() {
        runner.withPropertyValues("max.notification.token=")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(MaxNotificationClient.class);
                });
    }

    @Test
    void userProvidedClientBean_wins_conditionalOnMissingBeanBacksOff() {
        MaxNotificationClient custom = new MaxNotificationClient(
                RestClient.create(), "custom-token");

        runner.withPropertyValues("max.notification.token=some-token")
                .withBean(MaxNotificationClient.class, () -> custom)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(MaxNotificationClient.class);
                    assertThat(context.getBean(MaxNotificationClient.class)).isSameAs(custom);
                });
    }
}