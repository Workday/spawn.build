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

import build.base.configuration.Configuration;
import build.spawn.docker.jdk.HttpTransport;
import build.spawn.docker.option.DockerOption;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

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
    protected HttpTransport.Request createRequest() {
        final var node = this.objectMapper.createObjectNode();
        node.put("Name", this.name);
        node.put("CheckDuplicate", true);

        this.configuration.stream(DockerOption.class)
            .forEach(option -> option.configure(node, this.objectMapper));

        return HttpTransport.Request
            .post("/networks/create", node.toString().getBytes(StandardCharsets.UTF_8))
            .withContentType("application/json");
    }

    @Override
    protected Optional<String> createResult(final HttpTransport.Response response)
        throws IOException {

        if (response.statusCode() == 201) {
            return Optional.of(this.name);
        }
        return Optional.empty();
    }
}
