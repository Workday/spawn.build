package build.spawn.docker.okhttp.command;

/*-
 * #%L
 * Spawn Docker (OkHttp Client)
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

import build.base.flow.Publicist;
import build.base.flow.Subscriber;
import build.base.naming.UniqueNameGenerator;
import build.spawn.docker.Event;
import build.spawn.docker.okhttp.event.StatusEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

/**
 * The {@code Docker Daemon} {@link Command} to request a stream of system events using the
 * <a href="https://docs.docker.com/engine/api/v1.41/#operation/SystemEvents">System Events</a> command.
 *
 * @author brian.oliver
 * @since Jun-2021
 */
public class GetSystemEvents
    extends AbstractNonBlockingCommand<Void> {

    /**
     * The {@link ObjectMapper} for parsing json.
     */
    @Inject
    private ObjectMapper objectMapper;

    /**
     * The {@link Publicist} for {@code Docker Engine} {@link Event}s.
     */
    @Inject
    private Publicist<Event> publisher;

    @Override
    protected Request createRequest(final HttpUrl.Builder httpUrlBuilder) {
        return new Request.Builder()
            .url(httpUrlBuilder
                .addPathSegment("events")
                .build())
            .build();
    }

    @Override
    protected Void createResult(final Response response) {

        // create a unique name for the GetSystemEvents thread
        final var uniqueNameGenerator = new UniqueNameGenerator(".");
        final var name = uniqueNameGenerator.next();

        // create a Thread to commence reading the event stream from the response
        final Runnable runnable = () -> {
            // establish the Json-based Subscriber for System Events
            final var jsonSubscriber = new Subscriber<JsonNode>() {
                @Override
                public void onNext(final JsonNode item) {
                    // establish a Context to use for creating Events
                    final var context = createContext();
                    context.bind(JsonNode.class).to(item);

                    System.out.println("Raw Event: [" + name + "] " + item.toPrettyString());

                    // publish "status" events as StatusEvents
                    if (item.get("status") != null) {
                        final var event = context.create(StatusEvent.class);
                        GetSystemEvents.this.publisher.publish(event);
                    }
                }

                @Override
                public void onError(final Throwable throwable) {
                    GetSystemEvents.this.onProcessingError(throwable);
                }

                @Override
                public void onComplete() {
                    GetSystemEvents.this.onProcessingCompletion();
                }
            };

            // process the entire InputStream from the Response to essentially wait for the image to be created
            final var processor = new JsonNodeInputStreamProcessor(this.objectMapper);
            processor.process(response.body().byteStream(), jsonSubscriber);
        };

        Thread.ofVirtual()
            .name("docker-system-events-" + name)
            .start(runnable);

        return null;
    }

    @Override
    protected void onSuccessfulRequest(final Request request,
                                       final Response response,
                                       final Void result) {

        // no result to capture for a Void command — processing continues via the virtual thread above
    }
}
