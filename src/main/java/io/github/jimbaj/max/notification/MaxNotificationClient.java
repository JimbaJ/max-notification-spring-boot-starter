package io.github.jimbaj.max.notification;

import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MaxNotificationClient {

    private final Map<String, String> keyboardMessageByChat = new ConcurrentHashMap<>();

    private final RestClient restClient;
    private final String token;

    MaxNotificationClient(RestClient restClient, String token) {
        this.restClient = restClient;
        this.token = token;
    }

    public void send(String chatId, String text) {
        send(chatId, MaxMessage.text(text));
    }

    /**
     * chat_id — параметр строки запроса, в теле только текст.
     */
    public String send(String chatId, MaxMessage message) {
        Map<String, Object> body = newMessageBody(message);

        MaxSendResponse response = restClient.post()
                .uri(uri -> uri.path("/messages").queryParam("chat_id", chatId).build())
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(MaxSendResponse.class);


        String mid = response == null || response.message() == null || response.message().body() == null
                ? null
                : response.message().body().mid();
        if (mid == null) {
            throw new IllegalStateException("MAX вернул сообщение без mid");
        }
        return mid;
    }

    public void answerCallback(String callbackId, MaxMessage message) {
        MaxActionResponse response = restClient.post()
                .uri(uri -> uri.path("/answers").queryParam("callback_id", callbackId).build())
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("message", newMessageBody(message)))
                .retrieve()
                .body(MaxActionResponse.class);
        if (response == null || !response.success()) {
            throw new IllegalStateException("MAX отклонил ответ на нажатие: "
                    + (response == null ? "пустой ответ" : response.message()));
        }
    }

    public void editMessage(String messageId, MaxMessage message) {
        restClient.put()
                .uri(uri -> uri.path("/messages").queryParam("message_id", messageId).build())
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(newMessageBody(message))
                .retrieve()
                .toBodilessEntity();
    }

    public void clearKeyboard(String messageId) {
        restClient.put()
                .uri(uri -> uri.path("/messages").queryParam("message_id", messageId).build())
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("attachments", List.of()))
                .retrieve()
                .toBodilessEntity();
    }

    public String sendWithKeyboard(String chatId, MaxMessage message) {
        if (message.keyboard() == null) {
            return send(chatId, message);
        }
        return keyboardMessageByChat.compute(chatId, (chat, previousMid) -> {
            if (previousMid != null) {
                clearKeyboard(previousMid);
            }
            return send(chat, message);
        });
    }

    private Map<String, Object> newMessageBody(MaxMessage message) {
        Map<String, Object> body = new LinkedHashMap<>();
        if (message.text() != null) body.put("text", message.text());
        if (message.format() != null) body.put("format", message.format().value());
        if (message.keyboard() != null) {
            body.put("attachments", List.of(Map.of("type", "inline_keyboard", "payload", message.keyboard())));
        }
        return body;
    }
}