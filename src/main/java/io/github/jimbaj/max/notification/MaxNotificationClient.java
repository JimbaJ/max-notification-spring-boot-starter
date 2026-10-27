package io.github.jimbaj.max.notification;

import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.Map;

public class MaxNotificationClient {

    private final RestClient restClient;
    private final String token;

    MaxNotificationClient(RestClient restClient, String token) {
        this.restClient = restClient;
        this.token = token;
    }

    /** chat_id — параметр строки запроса, в теле только текст. */
    public void send(String chatId, String text) {
        restClient.post()
                .uri(uri -> uri.path("/messages").queryParam("chat_id", chatId).build())
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("text", text))
                .retrieve()
                .toBodilessEntity();
    }
}