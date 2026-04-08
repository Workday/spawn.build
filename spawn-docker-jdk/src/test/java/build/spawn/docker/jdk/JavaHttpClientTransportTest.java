package build.spawn.docker.jdk;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class JavaHttpClientTransportTest {

    private HttpServer server;
    private JavaHttpClientTransport transport;

    @BeforeEach
    void setUp() throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(0), 0);
        this.server.start();
        final var port = this.server.getAddress().getPort();
        final var httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
        this.transport = new JavaHttpClientTransport(httpClient, "http://localhost:" + port);
    }

    @AfterEach
    void tearDown() {
        this.server.stop(0);
    }

    @Test
    void shouldExecuteGetRequest() throws IOException {
        this.server.createContext("/ping", exchange -> {
            final var body = "pong".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });

        try (final var response = this.transport.execute(HttpTransport.Request.get("/ping"))) {
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.bodyString()).isEqualTo("pong");
        }
    }

    @Test
    void shouldExecutePostWithBody() throws IOException {
        this.server.createContext("/echo", exchange -> {
            final var received = exchange.getRequestBody().readAllBytes();
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, received.length);
            exchange.getResponseBody().write(received);
            exchange.close();
        });

        final var body = "hello".getBytes(StandardCharsets.UTF_8);
        try (final var response = this.transport.execute(HttpTransport.Request.post("/echo", body))) {
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.bodyString()).isEqualTo("hello");
        }
    }

    @Test
    void shouldReportNon2xxAsNotSuccessful() throws IOException {
        this.server.createContext("/fail", exchange -> {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        });

        try (final var response = this.transport.execute(HttpTransport.Request.get("/fail"))) {
            assertThat(response.isSuccessful()).isFalse();
            assertThat(response.statusCode()).isEqualTo(404);
        }
    }
}
