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

import build.base.configuration.Configuration;
import build.base.flow.CompletingSubscriber;
import build.base.flow.Publisher;
import build.spawn.docker.Event;
import build.spawn.docker.okhttp.Authenticator;
import build.spawn.docker.okhttp.event.StatusEvent;
import build.spawn.docker.option.ImageName;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * The {@code Docker Daemon} {@link Command} to pull an image using the
 * <a href="https://docs.docker.com/engine/api/v1.41/#operation/ImageCreate">Create Image</a> command.
 *
 * @author brian.oliver
 * @since Jun-2021
 */
public class PullImage
    extends AbstractEventBasedBlockingCommand<Optional<String>> {

    /**
     * The {@link ObjectMapper} for parsing json.
     */
    @Inject
    private ObjectMapper objectMapper;

    /**
     * The {@link Authenticator} for the {@link Request}.
     */
    @Inject
    private Authenticator authenticator;

    /**
     * The {@link CompletingSubscriber} for {@code Docker Engine} {@link Event}s.
     */
    @Inject
    private CompletingSubscriber<Event> eventSubscriber;

    /**
     * The name or id of the image to inspect.
     */
    private final String nameOrId;

    /**
     * The {@link Configuration}s for the image to pull.
     */
    private final Configuration configuration;

    /**
     * Constructs a {@link PullImage} {@link Command}.
     *
     * @param nameOrId      the name or id of the image to pull
     * @param configuration the {@link Configuration} for the image to pull
     */
    public PullImage(final String nameOrId, final Configuration configuration) {
        this.nameOrId = nameOrId;
        this.configuration = configuration == null
            ? Configuration.empty()
            : configuration;
    }

    @Override
    protected Request createRequest(final HttpUrl.Builder httpUrlBuilder) {
        final var names = ImageName.namesWithDockerRegistry(this.nameOrId, this.configuration);
        final var imageName = String.join("/", names);
        return this.authenticator.apply(new Request.Builder()
                .url(httpUrlBuilder
                    .addPathSegment("images")
                    .addPathSegment("create")
                    .build())
                .post(new FormBody.Builder()
                    .add("fromImage", imageName)
                    .build()))
            .build();
    }

    @Override
    protected Optional<String> createResult(final Response response) {
        return Optional.of(this.nameOrId);
    }

    @Override
    protected CompletableFuture<?> subscribe(final Publisher<Event> publisher) {
        return this.eventSubscriber.when(event ->
            event instanceof StatusEvent statusEvent
                && statusEvent.status().equals("pull")
                && statusEvent.jsonNode().get("id").asText().equals(this.nameOrId));
    }
}
