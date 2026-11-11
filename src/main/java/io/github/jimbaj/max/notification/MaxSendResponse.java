package io.github.jimbaj.max.notification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
record MaxSendResponse(Message message) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Message(Body body) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Body(String mid) {}
}