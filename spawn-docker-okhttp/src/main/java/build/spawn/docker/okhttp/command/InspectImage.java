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
import build.spawn.docker.Image;
import build.spawn.docker.okhttp.model.ImageInformation;
import build.spawn.docker.option.ImageName;
import jakarta.inject.Inject;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@code Docker Engine} {@link Command} to
 * <a href="https://docs.docker.com/engine/api/v1.41/#operation/ImageInspect">Inspect</a> an image.
 *
 * @author brian.oliver
 * @since Jun-2021
 */
public class InspectImage
    extends AbstractBlockingCommand<Optional<Image.Information>> {

    /**
     * The {@link ObjectMapper} for parsing json.
     */
    @Inject
    private ObjectMapper objectMapper;

    /**
     * The name or id of the {@link Image} to inspect.
     */
    private final String nameOrId;

    /**
     * The {@link Configuration} for this request.
     */
    private final Configuration configuration;

    /**
     * Constructs an {@link InspectImage} {@link Command}.
     *
     * @param nameOrId      the name or identity of the image to inspect
     * @param configuration the {@link Configuration}
     */
    public InspectImage(final String nameOrId, final Configuration configuration) {
        this.nameOrId = Objects.requireNonNull(nameOrId, "The image name or identity must not be null");
        this.configuration = configuration == null
            ? Configuration.empty()
            : configuration;
    }

    /**
     * Constructs an {@link InspectImage} {@link Command}.
     *
     * @param nameOrId the name or identity of the image to inspect
     */
    public InspectImage(final String nameOrId) {
        this(nameOrId, Configuration.empty());
    }

    @Override
    protected Request createRequest(final HttpUrl.Builder httpUrlBuilder) {
        final var registryName = ImageName.namesWithDockerRegistry(this.nameOrId, this.configuration);
        httpUrlBuilder.addPathSegment("images");
        registryName.forEach(httpUrlBuilder::addPathSegment);

        final var httpUrl = httpUrlBuilder
            .addPathSegment("json")
            .build();

        return new Request.Builder()
            .url(httpUrl)
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
    protected Optional<Image.Information> createResult(final Response response)
        throws IOException {
        if (response.code() == 404) {
            return Optional.empty();
        }

        // establish a new Context to create the Result
        final var context = createContext();

        // bind the JsonNode representation of the response
        final String json = response.body().string();
        context.bind(JsonNode.class).to(this.objectMapper.readTree(json));

        return Optional.of(context.create(ImageInformation.class));
    }
}
