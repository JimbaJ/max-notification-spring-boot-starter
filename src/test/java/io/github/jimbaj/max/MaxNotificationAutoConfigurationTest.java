package io.github.jimbaj.max;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

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
}