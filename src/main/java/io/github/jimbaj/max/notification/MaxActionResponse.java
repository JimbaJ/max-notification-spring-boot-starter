package io.github.jimbaj.max.notification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
record MaxActionResponse(boolean success, String message) {}
