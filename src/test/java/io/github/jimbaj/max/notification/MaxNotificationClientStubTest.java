package io.github.jimbaj.max.notification;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class MaxNotificationClientStubTest {

    private static final String TOKEN = "test-token";

    private HttpServer server;

    private final List<RecordedRequest> requests = new ArrayList<>();
    private final AtomicReference<StubResponse> stubResponse =
            new AtomicReference<>(new StubResponse(200, "{\"message\":{\"body\":{\"mid\":\"m-123\"}}}"));

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(MaxNotificationAutoConfiguration.class);

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);

        server.createContext("/", exchange -> {
            try (exchange) {
                requests.add(record(exchange));
                StubResponse response = stubResponse.get();
                byte[] body = response.body().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(response.status(), body.length);
                exchange.getResponseBody().write(body);
            }
        });

        server.start();
    }

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
        requests.clear();
    }

    @Test
    void send_postsToMessagesWithChatIdAndToken_andReturnsMidFromStub() {
        int port = server.getAddress().getPort();
        String apiUrl = "http://localhost:" + port;

        runner.withPropertyValues(
                        "max.notification.token=" + TOKEN,
                        "max.notification.api-url=" + apiUrl)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(MaxNotificationClient.class);

                    MaxNotificationClient client = context.getBean(MaxNotificationClient.class);
                    MaxMessage message = MaxMessage.text("hi");
                    String mid = client.send("42", message);

                    assertThat(mid).isEqualTo("m-123");
                });

        assertThat(requests).hasSize(1);
        RecordedRequest request = requests.getFirst();

        assertThat(request.method()).isEqualTo("POST");
        assertThat(request.pathWithQuery()).isEqualTo("/messages?chat_id=42");
        assertThat(request.authorization()).isEqualTo(TOKEN);
        assertThat(request.body()).contains("\"text\"").contains("hi");
        assertThat(request.body()).doesNotContain("chat_id");

    }

    @Test
    void send_responseWithoutMid_throws() {
        stubResponse.set(new StubResponse(200, "{\"message\":{\"body\":{}}}"));

        MaxNotificationClient client = clientFromContext();

        assertThatThrownBy(() -> client.send("42", MaxMessage.text("hi")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mid");
    }

    @Test
    void send_serverError_throws() {
        stubResponse.set(new StubResponse(500, "{}"));

        MaxNotificationClient client = clientFromContext();

        assertThatThrownBy(() -> client.send("42", MaxMessage.text("hi")))
                .isInstanceOf(RestClientResponseException.class);
    }

    @Test
    void answerCallback_successFalse_throws() {
        stubResponse.set(new StubResponse(200, "{\"success\":false,\"message\":\"nope\"}"));

        MaxNotificationClient client = clientFromContext();

        assertThatThrownBy(() -> client.answerCallback("cb-1", MaxMessage.text("ok")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nope");
    }

    @Test
    void editMessage_usesPutAndMessageIdQueryParam() {
        stubResponse.set(new StubResponse(200, "{}"));

        MaxNotificationClient client = clientFromContext();

        client.editMessage("msg-7", MaxMessage.text("edited"));

        assertThat(requests).hasSize(1);
        RecordedRequest request = requests.getFirst();

        assertThat(request.method()).isEqualTo("PUT");
        assertThat(request.pathWithQuery()).isEqualTo("/messages?message_id=msg-7");
        assertThat(request.authorization()).isEqualTo(TOKEN);
        assertThat(request.body()).contains("\"text\"").contains("edited");
    }

    @Test
    void clearKeyboard_sendsEmptyAttachmentsArray() {
        stubResponse.set(new StubResponse(200, "{}"));

        MaxNotificationClient client = clientFromContext();

        client.clearKeyboard("msg-9");

        assertThat(requests).hasSize(1);
        RecordedRequest request = requests.getFirst();

        assertThat(request.method()).isEqualTo("PUT");
        assertThat(request.pathWithQuery()).isEqualTo("/messages?message_id=msg-9");
        assertThat(request.body()).contains("\"attachments\"").contains("[]");
    }

    @Test
    void sendWithKeyboard_secondSendInSameChat_clearsPreviousKeyboard_andOtherChatDoesNot() {
        stubResponse.set(new StubResponse(200, "{\"message\":{\"body\":{\"mid\":\"m-123\"}}}"));

        MaxNotificationClient client = clientFromContext();

        MaxMessage withKeyboard = MaxMessage.text("hi").withKeyboard(keyboard());

        // 1) первый чат, первая отправка — только POST
        client.sendWithKeyboard("42", withKeyboard);

        // 2) тот же чат, вторая отправка — сначала PUT (снять клавиатуру), потом POST
        client.sendWithKeyboard("42", withKeyboard);

        // 3) другой чат — только POST, чужую клавиатуру не трогаем
        client.sendWithKeyboard("99", withKeyboard);

        assertThat(requests).hasSize(4);

        // 1) POST /messages?chat_id=42
        assertThat(requests.get(0).method()).isEqualTo("POST");
        assertThat(requests.get(0).pathWithQuery()).isEqualTo("/messages?chat_id=42");

        // 2) PUT /messages?message_id=m-123  — снимает клавиатуру у предыдущего сообщения
        assertThat(requests.get(1).method()).isEqualTo("PUT");
        assertThat(requests.get(1).pathWithQuery()).isEqualTo("/messages?message_id=m-123");
        assertThat(requests.get(1).body()).contains("\"attachments\"").contains("[]");

        // 3) POST /messages?chat_id=42  — вторая отправка
        assertThat(requests.get(2).method()).isEqualTo("POST");
        assertThat(requests.get(2).pathWithQuery()).isEqualTo("/messages?chat_id=42");

        // 4) POST /messages?chat_id=99  — другой чат, PUT перед ним не было
        assertThat(requests.get(3).method()).isEqualTo("POST");
        assertThat(requests.get(3).pathWithQuery()).isEqualTo("/messages?chat_id=99");
    }

    private MaxNotificationClient clientFromContext() {
        String apiUrl = "http://localhost:" + server.getAddress().getPort();
        AtomicReference<MaxNotificationClient> ref = new AtomicReference<>();
        runner.withPropertyValues(
                        "max.notification.token=" + TOKEN,
                        "max.notification.api-url=" + apiUrl)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(MaxNotificationClient.class);
                    ref.set(context.getBean(MaxNotificationClient.class));
                });
        return ref.get();
    }

    private static RecordedRequest record(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String pathWithQuery = exchange.getRequestURI().toString();
        String authorization = exchange.getRequestHeaders().getFirst("Authorization");
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        return new RecordedRequest(method, pathWithQuery, authorization, body);
    }

    private static MaxKeyboard keyboard() {
        return new MaxKeyboard(List.of(
                List.of(
                        MaxKeyboard.Button.callback("Yes", "yes"),
                        MaxKeyboard.Button.callback("No", "no")
                )
        ));
    }

    private record RecordedRequest(String method, String pathWithQuery,
                                   String authorization, String body) {
    }

    private record StubResponse(int status, String body) {
    }
}
