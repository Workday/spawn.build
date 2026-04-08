package build.spawn.docker.jdk;

/*-
 * #%L
 * Spawn Docker (JDK Client)
 * %%
 * Copyright (C) 2026 Workday, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * An {@link HttpTransport} implementation that communicates with the Docker daemon over a Unix domain socket.
 * <p>
 * Uses {@link SocketChannel} with {@link UnixDomainSocketAddress} (standard since Java 16) to establish
 * the socket connection and implements HTTP/1.1 framing directly.
 *
 * @author brian.oliver
 * @since Apr-2026
 */
public class UnixSocketHttpTransport
    implements HttpTransport {

    /**
     * The Unix domain socket {@link File}.
     */
    private final File socketFile;

    /**
     * Constructs a {@link UnixSocketHttpTransport}.
     *
     * @param socketFile the Unix domain socket {@link File}
     */
    public UnixSocketHttpTransport(final File socketFile) {
        this.socketFile = Objects.requireNonNull(socketFile, "The socket file must not be null");
    }

    @Override
    public Response execute(final Request request) throws IOException {
        final var channel = SocketChannel.open(UnixDomainSocketAddress.of(this.socketFile.toPath()));

        final var out = new BufferedOutputStream(Channels.newOutputStream(channel));
        writeRequest(out, request);
        out.flush();

        return Http11Parser.parse(new BufferedInputStream(Channels.newInputStream(channel)), channel);
    }

    /**
     * Writes an HTTP/1.1 request to the specified {@link OutputStream}.
     *
     * @param out     the {@link OutputStream}
     * @param request the {@link Request}
     * @throws IOException when writing fails
     */
    private void writeRequest(final OutputStream out, final Request request) throws IOException {
        writeLine(out, request.method() + " " + request.path() + " HTTP/1.1");
        writeLine(out, "Host: docker");
        writeLine(out, "Connection: close");

        final var body = request.body();
        writeLine(out, "Content-Length: " + (body != null ? body.length : 0));

        for (final Map.Entry<String, String> header : request.headers().entrySet()) {
            writeLine(out, header.getKey() + ": " + header.getValue());
        }

        writeLine(out, "");

        if (body != null && body.length > 0) {
            out.write(body);
        }
    }

    /**
     * Writes a CRLF-terminated line to the {@link OutputStream}.
     *
     * @param out  the {@link OutputStream}
     * @param line the line content
     * @throws IOException when writing fails
     */
    private void writeLine(final OutputStream out, final String line) throws IOException {
        out.write((line + "\r\n").getBytes(StandardCharsets.UTF_8));
    }
}
