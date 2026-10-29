package io.github.jimbaj.max.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MaxWebhookControllerTest {

    private static final String SECRET = "s3cr3t";
    private static final String SECRET_HEADER = "X-Max-Bot-Api-Secret";
    private static final String WEBHOOK_PATH = "/max/webhook";

    private MaxUpdateHandler handler;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        handler = mock(MaxUpdateHandler.class);

        MaxNotificationProperties properties = new MaxNotificationProperties();
        properties.getWebhook().setSecret(SECRET);
        properties.getWebhook().setPath(WEBHOOK_PATH);

        MaxWebhookSecretFilter filter = new MaxWebhookSecretFilter(properties);
        MaxWebhookController controller = new MaxWebhookController(handler);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addFilters(filter)
                .build();
    }

    @Test
    void messageCreated_withValidSecret_returns200AndInvokesHandler() throws Exception {
        String body = """
                {
                  "update_type": "message_created",
                  "timestamp": 1700000000000,
                  "message": {
                    "sender": { "user_id": 1, "first_name": "Alice", "is_bot": false, "username": "alice" },
                    "recipient": { "chat_id": 42, "chat_type": "dialog" },
                    "timestamp": 1700000000000,
                    "body": { "mid": "m1", "seq": 1, "text": "hi" }
                  }
                }
                """;

        mockMvc.perform(post(WEBHOOK_PATH)
                        .header(SECRET_HEADER, SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(handler, times(1)).onUpdate(any(MaxUpdate.class));
    }

    @Test
    void otherUpdateType_returns200AndDoesNotInvokeHandler() throws Exception {
        String body = """
                { "update_type": "bot_added", "timestamp": 1700000000000 }
                """;

        mockMvc.perform(post(WEBHOOK_PATH)
                        .header(SECRET_HEADER, SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(handler, never()).onUpdate(any());
    }

    @Test
    void emptyBody_returns200AndDoesNotInvokeHandler() throws Exception {
        mockMvc.perform(post(WEBHOOK_PATH)
                        .header(SECRET_HEADER, SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isOk());

        verify(handler, never()).onUpdate(any());
    }

    @Test
    void wrongSecret_returns403AndDoesNotInvokeHandler() throws Exception {
        String body = """
                {
                  "update_type": "message_created",
                  "timestamp": 1700000000000,
                  "message": {
                    "sender": { "user_id": 1 },
                    "recipient": { "chat_id": 42, "chat_type": "dialog" },
                    "timestamp": 1700000000000,
                    "body": { "mid": "m1", "seq": 1, "text": "hi" }
                  }
                }
                """;

        mockMvc.perform(post(WEBHOOK_PATH)
                        .header(SECRET_HEADER, "wrong")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        verify(handler, never()).onUpdate(any());
    }

    @Test
    void handlerThrowsException_stillReturns200() throws Exception {
        doThrow(new RuntimeException("boom")).when(handler).onUpdate(any());

        String body = """
                {
                  "update_type": "message_created",
                  "timestamp": 1700000000000,
                  "message": {
                    "sender": { "user_id": 1 },
                    "recipient": { "chat_id": 42, "chat_type": "dialog" },
                    "timestamp": 1700000000000,
                    "body": { "mid": "m1", "seq": 1, "text": "hi" }
                  }
                }
                """;

        mockMvc.perform(post(WEBHOOK_PATH)
                        .header(SECRET_HEADER, SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        verify(handler, times(1)).onUpdate(any(MaxUpdate.class));
    }
}