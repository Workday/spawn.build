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
import build.base.io.Terminal;
import build.spawn.docker.Container;
import build.spawn.docker.Execution;
import build.spawn.docker.jdk.HttpTransport;
import build.spawn.docker.option.DockerOption;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * The {@code Docker Daemon} {@link Command} to create an {@code exec} {@code Execution} for an {@link Container} using
 * the <a href="https://docs.docker.com/engine/api/v1.41/#operation/ContainerExec">Create Execution</a> command.
 *
 * @author brian.oliver
 * @since Sep-2021
 */
public class CreateExecution
    extends AbstractBlockingCommand<String> {

    /**
     * The {@link ObjectMapper} for parsing json.
     */
    @Inject
    private ObjectMapper objectMapper;

    /**
     * The {@link Container} in which the {@code exec} will be created.
     */
    private final Container container;

    /**
     * The {@link Configuration} required for the {@link Execution}.
     */
    private final Configuration configuration;

    /**
     * Is a {@link Terminal} required for the {@link Execution}.
     */
    private final boolean terminalRequired;

    /**
     * Constructs a {@link CreateExecution} {@link Command}.
     *
     * @param container        the {@link Container} for which the {@link Container} will be created
     * @param terminalRequired is a {@link Terminal} required for the {@link CreateExecution}
     * @param configuration    the {@link Configuration} for creating the {@link CreateExecution}
     */
    public CreateExecution(final Container container,
                           final boolean terminalRequired,
                           final Configuration configuration) {

        this.container = Objects.requireNonNull(container, "The Container must not be null");
        this.configuration = configuration;
        this.terminalRequired = terminalRequired;
    }

    @Override
    protected HttpTransport.Request createRequest() {

        // establish an ObjectNode containing the containers/create json
        final ObjectNode node = this.objectMapper.createObjectNode();
        node.put("AttachStdin", false);
        node.put("AttachStdout", this.terminalRequired);
        node.put("AttachStderr", this.terminalRequired);
        node.put("Tty", false);

        // allow the DockerOptions to configure the ObjectNode
        this.configuration.stream(DockerOption.class)
            .forEach(option -> option.configure(node, this.objectMapper));

        return HttpTransport.Request
            .post("/containers/" + this.container.id() + "/exec",
                node.toString().getBytes(StandardCharsets.UTF_8))
            .withContentType("application/json");
    }

    @Override
    protected String createResult(final HttpTransport.Response response)
        throws IOException {

        // bind the JsonNode representation of the response
        final String json = response.bodyString();
        final JsonNode node = this.objectMapper.readTree(json);

        // obtain the Execution identity to return
        return node.get("Id").asText();
    }
}
