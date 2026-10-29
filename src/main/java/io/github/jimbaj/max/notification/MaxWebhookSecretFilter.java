package io.github.jimbaj.max.notification;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class MaxWebhookSecretFilter implements Filter {

    private static final String SECRET_HEADER = "X-Max-Bot-Api-Secret";

    private final MaxNotificationProperties properties;

    public MaxWebhookSecretFilter(MaxNotificationProperties properties) {
        this.properties = properties;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String expected = properties.getWebhook().getSecret();
        String actual = request.getHeader(SECRET_HEADER);


        if (expected == null || expected.isEmpty()
                || actual == null
                || !MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return; // chain.doFilter НЕ вызывается
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

}
