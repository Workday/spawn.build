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
import build.spawn.docker.option.DockerOption;
import jakarta.inject.Inject;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A command to create docker network.
 *
 * @author anand.sankaran
 * @since Aug-2022
 */
public class CreateNetwork
    extends AbstractBlockingCommand<Optional<String>> {

    /**
     * The object mapper.
     */
    @Inject
    private ObjectMapper objectMapper;

    /**
     * Options to configure the network.
     */
    private final Configuration configuration;

    /**
     * Name of the network.
     */
    private final String name;

    /**
     * Constructor to create network.
     *
     * @param name          name of the network
     * @param configuration the {@link Configuration}
     */
    public CreateNetwork(final String name, final Configuration configuration) {
        this.name = name;
        this.configuration = configuration == null
            ? Configuration.empty()
            : configuration;
    }

    @Override
    protected Request createRequest(final HttpUrl.Builder httpUrlBuilder) {
        final var node = this.objectMapper.createObjectNode();
        node.put("Name", this.name);
        node.put("CheckDuplicate", true);

        this.configuration.stream(DockerOption.class)
            .forEach(option -> option.configure(node, this.objectMapper));

        return new Request.Builder()
            .url(httpUrlBuilder
                .addPathSegment("networks")
                .addPathSegment("create")
                .build())
            .post(RequestBody.create(node.toString(), MEDIA_TYPE_JSON))
            .build();
    }

    @Override
    protected Optional<String> createResult(final Response response)
        throws IOException {

        if (response.code() == 201) {
            return Optional.of(this.name);
        }
        return Optional.empty();
    }
}
