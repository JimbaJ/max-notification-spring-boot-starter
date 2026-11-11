package io.github.jimbaj.max.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MaxKeyboard(@JsonProperty("buttons") List<List<Button>> rows) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Button(String type, String text, String payload, String url) {
        public static Button callback(String text, String payload) {
            return new Button("callback", text, payload, null);
        }
        public static Button link(String text, String url) {
            return new Button("link", text, null, url);
        }
    }
}