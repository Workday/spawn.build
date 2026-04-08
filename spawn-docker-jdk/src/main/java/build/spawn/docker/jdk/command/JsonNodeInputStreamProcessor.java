package build.spawn.docker.jdk.command;

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

import build.base.flow.Subscriber;
import build.base.flow.Subscription;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An {@link InputStreamProcessor} for {@link JsonNode}s.
 *
 * @author brian.oliver
 * @since Jun-2021
 */
public class JsonNodeInputStreamProcessor
    implements InputStreamProcessor<JsonNode> {

    /**
     * The {@link Logger}.
     */
    private static final Logger LOG = Logger.getLogger(JsonNodeInputStreamProcessor.class.getName());

    /**
     * The {@link JsonFactory} to produce {@link JsonParser}s.
     */
    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    /**
     * The {@link ObjectMapper}.
     */
    private final ObjectMapper objectMapper;

    /**
     * Construct a {@link JsonNodeInputStreamProcessor}.
     *
     * @param objectMapper the {@link ObjectMapper}
     */
    public JsonNodeInputStreamProcessor(final ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "The ObjectMapper must not be null");
    }

    @Override
    public void process(final InputStream inputStream,
                        final Subscriber<? super JsonNode> subscriber) {

        Objects.requireNonNull(inputStream, "The InputStream must not be null");
        Objects.requireNonNull(subscriber, "The Observer must not be null");

        boolean failed = false;
        try {
            // establish the JsonParser for the InputStream
            final var parser = JSON_FACTORY.createParser(inputStream);

            // notify the subscriber that is has been subscribed (with a no-op subscription)
            final var cancelled = new AtomicBoolean(false);

            final var subscription = new Subscription() {
                @Override
                public void request(final long number) {
                    // ignore the requests for a number of items
                }

                @Override
                public void cancel() {
                    cancelled.set(true);
                }
            };

            subscriber.onSubscribe(subscription);

            JsonToken token;
            while (!cancelled.get()
                && !failed
                && !parser.isClosed()
                && (token = parser.nextToken()) != null
                && token != JsonToken.END_OBJECT) {

                try {
                    final JsonNode jsonNode = this.objectMapper.readTree(parser);
                    subscriber.onNext(jsonNode);
                } catch (final Throwable throwable) {
                    LOG.log(Level.FINE, "Failed to read or deliver a JSON node from the stream", throwable);
                    failed = true;
                    subscriber.onError(throwable);
                }
            }
        } catch (final Throwable throwable) {
            LOG.log(Level.FINE, "Failed while processing the JSON input stream", throwable);
            if (!failed) {
                subscriber.onError(throwable);
            }
        } finally {
            try {
                if (!failed) {
                    subscriber.onComplete();
                }

                inputStream.close();
            } catch (final IOException e) {
                LOG.log(Level.FINE, "Failed to close the JSON input stream", e);
            }
        }
    }
}
