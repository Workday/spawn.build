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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

/**
 * An {@link HttpTransport} implementation that uses {@link HttpClient} to connect to Docker via TCP.
 *
 * @author brian.oliver
 * @since Apr-2026
 */
public class JavaHttpClientTransport
    implements HttpTransport {

    /**
     * The {@link HttpClient} to use for executing requests.
     */
    private final HttpClient httpClient;

    /**
     * The base URL of the Docker daemon (e.g. {@code http://localhost:2375}).
     */
    private final String baseUrl;

    /**
     * Constructs a {@link JavaHttpClientTransport}.
     *
     * @param httpClient the {@link HttpClient}
     * @param baseUrl    the base URL (e.g. {@code http://localhost:2375})
     */
    public JavaHttpClientTransport(final HttpClient httpClient, final String baseUrl) {
        this.httpClient = Objects.requireNonNull(httpClient, "The HttpClient must not be null");
        this.baseUrl = Objects.requireNonNull(baseUrl, "The base URL must not be null");
    }

    @Override
    public Response execute(final Request request) throws IOException {
        try {
            final var builder = HttpRequest.newBuilder()
                .uri(URI.create(this.baseUrl + request.path()));

            request.headers().forEach(builder::header);

            final var body = request.body();
            final var bodyPublisher = (body == null || body.length == 0)
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofByteArray(body);

            switch (request.method()) {
                case "GET" -> builder.GET();
                case "DELETE" -> builder.DELETE();
                case "HEAD" -> builder.method("HEAD", HttpRequest.BodyPublishers.noBody());
                case "PUT" -> builder.PUT(bodyPublisher);
                default -> builder.POST(bodyPublisher);
            }

            final var httpResponse = this.httpClient
                .send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());

            return new JavaHttpResponse(httpResponse);

        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }

    /**
     * An {@link HttpTransport.Response} backed by a {@link HttpResponse}.
     */
    private static final class JavaHttpResponse
        implements Response {

        private final HttpResponse<InputStream> response;

        JavaHttpResponse(final HttpResponse<InputStream> response) {
            this.response = response;
        }

        @Override
        public int statusCode() {
            return this.response.statusCode();
        }

        @Override
        public String header(final String name) {
            return this.response.headers().firstValue(name).orElse(null);
        }

        @Override
        public InputStream bodyStream() {
            return this.response.body();
        }

        @Override
        public void cancel() {
            close();
        }

        @Override
        public void close() {
            try {
                this.response.body().close();
            } catch (final IOException ignored) {
                // we ignore close failures
            }
        }
    }
}
