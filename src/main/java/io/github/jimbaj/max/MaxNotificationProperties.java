package io.github.jimbaj.max;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("max.notification")
public class MaxNotificationProperties {

    /** Токен бота из кабинета MAX. Пустой — стартер не включает клиента. */
    private String token;

    /** Адрес API; по умолчанию боевой. */
    private String apiUrl = "https://platform-api2.max.ru";

    /** Сколько миллисекунд ждать установки соединения. */
    private int connectTimeoutMs = 2000;

    /** Сколько миллисекунд ждать ответа. */
    private int readTimeoutMs = 5000;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }

    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }

    public int getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
}