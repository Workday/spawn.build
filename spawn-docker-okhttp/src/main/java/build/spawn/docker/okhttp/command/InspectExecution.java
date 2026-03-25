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

import build.spawn.docker.Execution;
import build.spawn.docker.okhttp.model.ExecutionInformation;
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
 * The {@code Docker Engine} {@link Command} to
 * <a href="https://docs.docker.com/engine/api/v1.41/#operation/ExecInspect">Inspect</a> an {@link Execution}.
 *
 * @author brian.oliver
 * @since Sep-2021
 */
public class InspectExecution
    extends AbstractBlockingCommand<Optional<Execution.Information>> {

    /**
     * The {@link ObjectMapper} for parsing json.
     */
    @Inject
    private ObjectMapper objectMapper;

    /**
     * The id of the {@link Execution} to inspect
     */
    private final String id;

    /**
     * Constructs an {@link InspectExecution} {@link Command}.
     *
     * @param id the identity of the {@link Execution} to inspect
     */
    public InspectExecution(final String id) {
        this.id = Objects.requireNonNull(id, "The execution identity must not be null");
    }

    @Override
    protected Request createRequest(final HttpUrl.Builder httpUrlBuilder) {
        return new Request.Builder()
            .url(httpUrlBuilder
                .addPathSegment("exec")
                .addPathSegment(this.id)
                .addPathSegment("json")
                .build())
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
    protected Optional<Execution.Information> createResult(final Response response)
        throws IOException {

        if (response.code() == 404) {
            return Optional.empty();
        }

        // establish a new Context to create the Result
        final var context = createContext();

        // bind the JsonNode representation of the response
        final var json = response.body().string();
        context.bind(JsonNode.class).to(this.objectMapper.readTree(json));

        return Optional.of(context.create(ExecutionInformation.class));
    }
}
