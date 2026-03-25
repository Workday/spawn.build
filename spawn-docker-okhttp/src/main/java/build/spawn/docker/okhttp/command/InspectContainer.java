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

import build.codemodel.injection.Context;
import build.spawn.docker.Container;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * The {@code Docker Daemon} {@link Command} to inspect a {@code Container} using the
 * <a href="https://docs.docker.com/engine/api/v1.41/#operation/ContainerInspect">Inspect Container</a> command.
 *
 * @author brian.oliver
 * @since Jul-2021
 */
public class InspectContainer
    extends AbstractBlockingCommand<Optional<Container.Information>> {

    /**
     * The {@link ObjectMapper} for parsing json.
     */
    @Inject
    private ObjectMapper objectMapper;

    /**
     * The {@link Container} to inspect.
     */
    private final Container container;

    /**
     * Constructs a {@link InspectContainer} {@link Command}.
     *
     * @param container the {@link Container} to inspect
     */
    public InspectContainer(final Container container) {
        this.container = Objects.requireNonNull(container, "The Container id must not be null");
    }

    @Override
    protected Request createRequest(final HttpUrl.Builder httpUrlBuilder) {
        return new Request.Builder()
            .url(httpUrlBuilder
                .addPathSegment("containers")
                .addPathSegment(this.container.id())
                .addPathSegment("json")
                .build())
            .get()
            .build();
    }

    @Override
    protected void onUnsuccessfulRequest(final Request request, final Response response)
        throws IOException {

        if (response.code() != 404) {
            super.onUnsuccessfulRequest(request, response);
        }
    }

    @Override
    protected Optional<Container.Information> createResult(final Response response)
        throws IOException {

        if (response.code() == 404) {
            return Optional.empty();
        }

        // establish a new Context to create the Container.Information
        final var context = createContext();
        context.bind(Container.class).to(this.container);
        context.bind(Context.class).to(context);

        // bind the JsonNode representation of the response
        final var json = response.body().string();
        context.bind(JsonNode.class).to(this.objectMapper.readTree(json));

        // establish the Container.Information based on the Response
        return Optional.of(context.create(build.spawn.docker.okhttp.model.ContainerInformation.class));
    }
}
