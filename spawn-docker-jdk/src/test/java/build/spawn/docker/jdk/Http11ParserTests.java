package build.spawn.docker.jdk;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class Http11ParserTests {

    @Test
    void shouldParseStatusCode200() throws IOException {
        final var raw = "HTTP/1.1 200 OK\r\nContent-Length: 0\r\n\r\n";
        final var response = Http11Parser.parse(stream(raw), () -> {});
        assertThat(response.statusCode()).isEqualTo(200);
        response.close();
    }

    @Test
    void shouldParseBodyWithContentLength() throws IOException {
        final var raw = "HTTP/1.1 200 OK\r\nContent-Length: 5\r\n\r\nhello";
        final var response = Http11Parser.parse(stream(raw), () -> {});
        assertThat(response.bodyString()).isEqualTo("hello");
        response.close();
    }

    @Test
    void shouldParseChunkedBody() throws IOException {
        final var raw = "HTTP/1.1 200 OK\r\nTransfer-Encoding: chunked\r\n\r\n5\r\nhello\r\n0\r\n\r\n";
        final var response = Http11Parser.parse(stream(raw), () -> {});
        assertThat(response.bodyString()).isEqualTo("hello");
        response.close();
    }

    @Test
    void shouldParse204NoContent() throws IOException {
        final var raw = "HTTP/1.1 204 No Content\r\n\r\n";
        final var response = Http11Parser.parse(stream(raw), () -> {});
        assertThat(response.statusCode()).isEqualTo(204);
        response.close();
    }

    @Test
    void shouldParseMultiChunkBody() throws IOException {
        final var raw = "HTTP/1.1 200 OK\r\nTransfer-Encoding: chunked\r\n\r\n3\r\nabc\r\n3\r\ndef\r\n0\r\n\r\n";
        final var response = Http11Parser.parse(stream(raw), () -> {});
        assertThat(response.bodyString()).isEqualTo("abcdef");
        response.close();
    }

    private static ByteArrayInputStream stream(final String s) {
        return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
    }
}
