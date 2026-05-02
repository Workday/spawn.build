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
import build.base.json.Json;
import build.base.json.JsonArray;
import build.base.json.JsonObject;
import build.spawn.docker.Container;
import build.spawn.docker.Execution;
import build.spawn.docker.jdk.HttpTransport;

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
        final var nodeBuilder = JsonObject.builder()
            .put("AttachStdin", false)
            .put("AttachStdout", this.terminalRequired)
            .put("AttachStderr", this.terminalRequired)
            .put("Tty", false);

        // Cmd — only Command options apply here
        this.configuration.stream(build.spawn.docker.option.Command.class)
            .findFirst()
            .ifPresent(cmd -> {
                final var arr = JsonArray.builder();
                cmd.values().forEach(arr::add);
                nodeBuilder.put("Cmd", arr.build());
            });

        return HttpTransport.Request
            .post("/containers/" + this.container.id() + "/exec",
                nodeBuilder.build().toJsonString().getBytes(StandardCharsets.UTF_8))
            .withContentType("application/json");
    }

    @Override
    protected String createResult(final HttpTransport.Response response)
        throws IOException {

        // obtain the Execution identity to return
        return Json.parse(response.bodyString()).getString("Id");
    }
}
