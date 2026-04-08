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

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parses HTTP/1.1 responses from an {@link InputStream}, handling {@code Content-Length} and
 * {@code Transfer-Encoding: chunked} bodies.
 *
 * @author brian.oliver
 * @since Apr-2026
 */
class Http11Parser {

    private Http11Parser() {
    }

    /**
     * Parses an HTTP/1.1 response from the specified {@link InputStream}.
     *
     * @param in         the {@link InputStream} to read from
     * @param connection a {@link AutoCloseable} representing the underlying connection, closed on {@link HttpTransport.Response#close()}
     * @return the parsed {@link HttpTransport.Response}
     * @throws IOException when the response cannot be parsed
     */
    static HttpTransport.Response parse(final InputStream in,
                                        final AutoCloseable connection) throws IOException {

        // parse the status line: "HTTP/1.1 200 OK"
        final var statusLine = readLine(in);
        final var parts = statusLine.split(" ", 3);
        final var statusCode = Integer.parseInt(parts[1]);

        // parse the headers
        final Map<String, String> headers = new LinkedHashMap<>();
        String headerLine;
        while (!(headerLine = readLine(in)).isEmpty()) {
            final var colon = headerLine.indexOf(':');
            if (colon > 0) {
                headers.put(headerLine.substring(0, colon).trim().toLowerCase(),
                    headerLine.substring(colon + 1).trim());
            }
        }

        // determine the body stream
        final var transferEncoding = headers.getOrDefault("transfer-encoding", "");
        final var contentLengthStr = headers.get("content-length");

        final InputStream body;
        if ("chunked".equalsIgnoreCase(transferEncoding)) {
            body = new ChunkedInputStream(in);
        } else if (contentLengthStr != null) {
            body = new BoundedInputStream(in, Long.parseLong(contentLengthStr));
        } else {
            body = in;
        }

        return new ParsedResponse(statusCode, headers, body, connection);
    }

    /**
     * Reads a CRLF-terminated line from the {@link InputStream}.
     *
     * @param in the {@link InputStream}
     * @return the line content without the CRLF
     * @throws IOException when reading fails
     */
    static String readLine(final InputStream in) throws IOException {
        final var sb = new StringBuilder();
        int b;
        while ((b = in.read()) != -1) {
            if (b == '\r') {
                final var next = in.read();
                if (next == '\n') {
                    break;
                }
                sb.append((char) b);
                if (next != -1) {
                    sb.append((char) next);
                }
            } else if (b == '\n') {
                break;
            } else {
                sb.append((char) b);
            }
        }
        return sb.toString();
    }

    /**
     * An {@link HttpTransport.Response} backed by a parsed HTTP/1.1 response.
     */
    private static final class ParsedResponse
        implements HttpTransport.Response {

        private final int statusCode;
        private final Map<String, String> headers;
        private final InputStream body;
        private final AutoCloseable connection;

        ParsedResponse(final int statusCode,
                       final Map<String, String> headers,
                       final InputStream body,
                       final AutoCloseable connection) {
            this.statusCode = statusCode;
            this.headers = headers;
            this.body = body;
            this.connection = connection;
        }

        @Override
        public int statusCode() {
            return this.statusCode;
        }

        @Override
        public String header(final String name) {
            return this.headers.get(name.toLowerCase());
        }

        @Override
        public InputStream bodyStream() {
            return this.body;
        }

        @Override
        public void cancel() {
            close();
        }

        @Override
        public void close() {
            try {
                this.connection.close();
            } catch (final Exception ignored) {
                // we ignore close failures
            }
        }
    }

    /**
     * An {@link InputStream} that decodes HTTP chunked transfer encoding.
     */
    static final class ChunkedInputStream
        extends InputStream {

        private final InputStream in;
        private int remaining;
        private boolean done;

        ChunkedInputStream(final InputStream in) {
            this.in = in;
            this.remaining = 0;
            this.done = false;
        }

        @Override
        public int read() throws IOException {
            final var buf = new byte[1];
            final var n = read(buf, 0, 1);
            return n == -1 ? -1 : buf[0] & 0xff;
        }

        @Override
        public int read(final byte[] buf, final int off, final int len) throws IOException {
            if (this.done) {
                return -1;
            }
            if (this.remaining == 0) {
                // read the chunk size line (hex digits, possibly with extensions)
                final var sizeLine = readLine(this.in).trim();
                if (sizeLine.isEmpty()) {
                    return -1;
                }
                // strip chunk extensions (anything after ';')
                final var semicolon = sizeLine.indexOf(';');
                final var hexSize = semicolon >= 0 ? sizeLine.substring(0, semicolon) : sizeLine;
                this.remaining = Integer.parseInt(hexSize.trim(), 16);
                if (this.remaining == 0) {
                    this.done = true;
                    return -1;
                }
            }
            final var toRead = Math.min(len, this.remaining);
            final var n = this.in.read(buf, off, toRead);
            if (n > 0) {
                this.remaining -= n;
                if (this.remaining == 0) {
                    // consume the trailing CRLF after the chunk data
                    readLine(this.in);
                }
            }
            return n;
        }
    }

    /**
     * An {@link InputStream} that reads at most a bounded number of bytes.
     */
    static final class BoundedInputStream
        extends InputStream {

        private final InputStream in;
        private long remaining;

        BoundedInputStream(final InputStream in, final long limit) {
            this.in = in;
            this.remaining = limit;
        }

        @Override
        public int read() throws IOException {
            if (this.remaining <= 0) {
                return -1;
            }
            final var b = this.in.read();
            if (b != -1) {
                this.remaining--;
            }
            return b;
        }

        @Override
        public int read(final byte[] buf, final int off, final int len) throws IOException {
            if (this.remaining <= 0) {
                return -1;
            }
            final var toRead = (int) Math.min(len, this.remaining);
            final var n = this.in.read(buf, off, toRead);
            if (n > 0) {
                this.remaining -= n;
            }
            return n;
        }
    }
}
