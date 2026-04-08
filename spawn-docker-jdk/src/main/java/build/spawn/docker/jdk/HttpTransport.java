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
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A transport abstraction over HTTP for communicating with the Docker Engine API.
 *
 * @author brian.oliver
 * @since Apr-2026
 */
public interface HttpTransport {

    /**
     * Executes the specified {@link Request} and returns a {@link Response}.
     * <p>
     * The caller is responsible for closing the {@link Response}.
     *
     * @param request the {@link Request} to execute
     * @return the {@link Response}
     * @throws IOException when the request fails
     */
    Response execute(Request request) throws IOException;

    /**
     * An HTTP request.
     *
     * @param method  the HTTP method (GET, POST, DELETE, HEAD)
     * @param path    the request path including any query parameters (e.g. "/containers/123/start?signal=SIGKILL")
     * @param headers the request headers
     * @param body    the request body bytes, or {@code null} for no body
     */
    record Request(String method, String path, Map<String, String> headers, byte[] body) {

        /**
         * Constructs a {@link Request} with a defensive copy of the headers map.
         */
        public Request {
            headers = new LinkedHashMap<>(headers == null ? Map.of() : headers);
        }

        /**
         * Returns a copy of this {@link Request} with an additional header.
         *
         * @param name  the header name
         * @param value the header value
         * @return a new {@link Request} with the header added
         */
        public Request withHeader(final String name, final String value) {
            final var copy = new LinkedHashMap<>(headers);
            copy.put(name, value);
            return new Request(method, path, copy, body);
        }

        /**
         * Returns a copy of this {@link Request} with a {@code Content-Type} header.
         *
         * @param contentType the content type
         * @return a new {@link Request}
         */
        public Request withContentType(final String contentType) {
            return withHeader("Content-Type", contentType);
        }

        /**
         * Creates a GET {@link Request} for the specified path.
         *
         * @param path the request path
         * @return a new GET {@link Request}
         */
        public static Request get(final String path) {
            return new Request("GET", path, Map.of(), null);
        }

        /**
         * Creates a POST {@link Request} for the specified path and body.
         *
         * @param path the request path
         * @param body the request body bytes, or {@code null} for an empty body
         * @return a new POST {@link Request}
         */
        public static Request post(final String path, final byte[] body) {
            return new Request("POST", path, Map.of(), body);
        }

        /**
         * Creates a DELETE {@link Request} for the specified path.
         *
         * @param path the request path
         * @return a new DELETE {@link Request}
         */
        public static Request delete(final String path) {
            return new Request("DELETE", path, Map.of(), null);
        }

        /**
         * Creates a PUT {@link Request} for the specified path and body.
         *
         * @param path the request path
         * @param body the request body bytes, or {@code null} for an empty body
         * @return a new PUT {@link Request}
         */
        public static Request put(final String path, final byte[] body) {
            return new Request("PUT", path, Map.of(), body);
        }

        /**
         * Creates a HEAD {@link Request} for the specified path.
         *
         * @param path the request path
         * @return a new HEAD {@link Request}
         */
        public static Request head(final String path) {
            return new Request("HEAD", path, Map.of(), null);
        }
    }

    /**
     * An HTTP response.
     */
    interface Response extends AutoCloseable {

        /**
         * Returns the HTTP status code.
         *
         * @return the status code
         */
        int statusCode();

        /**
         * Returns {@code true} when the status code is in the 2xx range.
         *
         * @return {@code true} if successful
         */
        default boolean isSuccessful() {
            return statusCode() >= 200 && statusCode() < 300;
        }

        /**
         * Returns the value of the specified response header, or {@code null} if absent.
         *
         * @param name the header name (case-insensitive)
         * @return the header value, or {@code null}
         */
        String header(String name);

        /**
         * Returns the response body as an {@link InputStream}.
         *
         * @return the body stream
         * @throws IOException when the body cannot be read
         */
        InputStream bodyStream() throws IOException;

        /**
         * Returns the complete response body as a {@link String}.
         *
         * @return the body string
         * @throws IOException when the body cannot be read
         */
        default String bodyString() throws IOException {
            return new String(bodyStream().readAllBytes(), StandardCharsets.UTF_8);
        }

        /**
         * Cancels any ongoing processing and releases resources.
         */
        void cancel();

        @Override
        void close();
    }
}
