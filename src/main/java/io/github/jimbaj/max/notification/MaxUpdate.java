package io.github.jimbaj.max.notification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MaxUpdate(
        @JsonProperty("update_type") String updateType,
        Long timestamp,
        Message message,
        Callback callback
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Message(
            User sender,
            Recipient recipient,
            Long timestamp,
            MessageBody body
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Callback(
            @JsonProperty("callback_id") Long callbackId,
            String payload
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record User(
            @JsonProperty("user_id") Long userId,
            @JsonProperty("first_name") String firstName,
            @JsonProperty("is_bot") Boolean isBot,
            String username
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Recipient(
            @JsonProperty("chat_id") Long chatId,
            @JsonProperty("chat_type") String chatType
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MessageBody(
            String mid,
            Long seq,
            String text
    ) {
    }
}