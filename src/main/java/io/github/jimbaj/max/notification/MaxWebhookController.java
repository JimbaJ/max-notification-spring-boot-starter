package io.github.jimbaj.max.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MaxWebhookController {

    private static final Logger log = LoggerFactory.getLogger(MaxWebhookController.class);
    private static final String UPDATE_MESSAGE_CREATED = "message_created";
    private static final String UPDATE_MESSAGE_CALLBACK = "message_callback";

    private final MaxUpdateHandler handler;

    public MaxWebhookController(MaxUpdateHandler handler) {
        this.handler = handler;
    }

    @PostMapping("${max.notification.webhook.path:/max/webhook}")
    public void onWebhook(@RequestBody(required = false) MaxUpdate update) {
        if (update == null) {
            return;
        }

        String type = update.updateType();
        if (!UPDATE_MESSAGE_CREATED.equals(type) && !UPDATE_MESSAGE_CALLBACK.equals(type)) {
            return;
        }

        try {
            handler.onUpdate(update);
        } catch (RuntimeException e) {
            log.warn("Обработчик не смог обработать update типа '{}': {}", type, e.getMessage(), e);
        }
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public void onUnreadableBody(HttpMessageNotReadableException e) {
        log.warn("Не удалось разобрать тело вебхука, отвечаем 200: {}", e.getMessage());
    }
}
