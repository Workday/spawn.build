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

import build.spawn.docker.Network;
import build.spawn.docker.okhttp.model.NetworkInformation;
import jakarta.inject.Inject;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A command to get network information.
 *
 * @author anand.sankaran
 * @since Aug-2022
 */
public class InspectNetwork
    extends AbstractBlockingCommand<Optional<Network.Information>> {

    private final String nameOrId;

    /**
     * Constructor.
     *
     * @param nameOrId name or id of the network.
     */
    public InspectNetwork(final String nameOrId) {
        this.nameOrId = nameOrId;
    }

    /**
     * The {@link ObjectMapper} for parsing json.
     */
    @Inject
    private ObjectMapper objectMapper;

    @Override
    protected Request createRequest(final HttpUrl.Builder httpUrlBuilder) {
        return new Request.Builder()
            .url(httpUrlBuilder
                .addPathSegment("networks")
                .addPathSegment(this.nameOrId)
                .build())
            .get()
            .build();
    }

    @Override
    protected Optional<Network.Information> createResult(final Response response)
        throws IOException {
        if (response.code() == 404) {
            return Optional.empty();
        }

        // establish a new Context to create the Result
        final var context = createContext();

        // bind the JsonNode representation of the response
        final var json = response.body().string();
        context.bind(JsonNode.class).to(this.objectMapper.readTree(json));

        return Optional.of(context.create(NetworkInformation.class));
    }
}
