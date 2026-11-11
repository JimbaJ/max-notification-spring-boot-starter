package io.github.jimbaj.max.notification;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MaxFormat {
    MARKDOWN("markdown"), HTML("html");
    private final String value;
    MaxFormat(String value) { this.value = value; }
    @JsonValue
    public String value() { return value; }
}
