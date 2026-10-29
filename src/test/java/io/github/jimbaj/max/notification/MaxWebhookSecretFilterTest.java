package io.github.jimbaj.max.notification;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class MaxWebhookSecretFilterTest {

    private static final String HEADER = "X-Max-Bot-Api-Secret";
    private static final String SECRET = "s3cr3t";

    private MaxWebhookSecretFilter filterWithSecret(String secret) {
        MaxNotificationProperties properties = new MaxNotificationProperties();
        properties.getWebhook().setSecret(secret);
        return new MaxWebhookSecretFilter(properties);
    }

    private MockHttpServletRequest requestWithHeader(String value) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/webhook");
        if (value != null) {
            request.addHeader(HEADER, value);
        }
        return request;
    }

    @Test
    void secretMatches_chainInvoked() throws Exception {
        MaxWebhookSecretFilter filter = filterWithSecret(SECRET);
        MockHttpServletRequest request = requestWithHeader(SECRET);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isNotEqualTo(HttpServletResponse.SC_FORBIDDEN);
        assertThat(chain.getRequest()).isNotNull(); // chain вызван
    }

    @Test
    void secretMismatch_403_chainNotInvoked() throws Exception {
        MaxWebhookSecretFilter filter = filterWithSecret(SECRET);
        MockHttpServletRequest request = requestWithHeader("wrong");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
        assertThat(chain.getRequest()).isNull(); // chain НЕ вызван
    }

    @Test
    void headerMissing_403() throws Exception {
        MaxWebhookSecretFilter filter = filterWithSecret(SECRET);
        MockHttpServletRequest request = requestWithHeader(null);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
        assertThat(chain.getRequest()).isNull();
    }

    @Test
    void expectedSecretNullOrEmpty_403_evenIfHeaderMatches() throws Exception {
        // прогон 1: secret == null
        assert403WhenExpectedIs(null);

        // прогон 2: secret == ""
        assert403WhenExpectedIs("");
    }

    private void assert403WhenExpectedIs(String expected) throws Exception {
        MaxWebhookSecretFilter filter = filterWithSecret(expected);
        // заголовок совпадает с ожидаемым (в т.ч. пустой/отсутствующий — не важно)
        MockHttpServletRequest request = requestWithHeader(expected);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
        assertThat(chain.getRequest()).isNull();
    }
}