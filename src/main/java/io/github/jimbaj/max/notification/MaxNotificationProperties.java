package io.github.jimbaj.max.notification;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("max.notification")
public class MaxNotificationProperties {

    /**
     * Токен бота из кабинета MAX. Пустой — стартер не включает клиента.
     */
    private String token;

    /**
     * Адрес API; по умолчанию боевой.
     */
    private String apiUrl = "https://platform-api2.max.ru";

    /**
     * Настройки входящего webhook.
     */
    private Webhook webhook = new Webhook();

    /**
     * Сколько миллисекунд ждать установки соединения.
     */
    private int connectTimeoutMs = 2000;

    /**
     * Сколько миллисекунд ждать ответа.
     */
    private int readTimeoutMs = 5000;

    public static class Webhook {
        /**
         * Путь HTTP-эндпоинта, на который MAX присылает события. По умолчанию {@code /max/webhook}.
         */
        private String path = "/max/webhook";
        /**
         * Секрет для проверки подлинности входящих запросов от MAX (передаётся в заголовке). Пустой — приёмник закрыт, всегда 403.
         */
        private String secret;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public Webhook getWebhook() {
        return webhook;
    }

    public void setWebhook(Webhook webhook) {
        this.webhook = webhook;
    }
}