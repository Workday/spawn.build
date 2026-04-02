package build.spawn.docker.option;

/*-
 * #%L
 * Spawn Docker (Client)
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@link DockerOption} to define the {@code Docker Engine} {@code CMD}.
 *
 * @author brian.oliver
 * @since Jun-2021
 */
public class Command
    implements DockerOption {

    /**
     * The values for the {@link Command}.
     */
    private final ArrayList<String> values;

    /**
     * Constructs a {@link Command}.
     *
     * @param values the values for the {@link Command}
     */
    private Command(final Stream<String> values) {
        this.values = values == null
            ? new ArrayList<>()
            : values.collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void configure(final ObjectNode objectNode, final ObjectMapper objectMapper) {

        // establish the "Cmd" for the Container
        final var arrayNode = objectNode.arrayNode();
        this.values.forEach(arrayNode::add);

        objectNode.set("Cmd", arrayNode);
    }

    /**
     * Constructs a {@link Command} with the provided values.
     *
     * @param command   the command
     * @param arguments the arguments
     * @return a new {@link Command}
     */
    public static Command of(final String command,
                             final String... arguments) {

        return new Command(Stream.concat(
            Stream.of(Objects.requireNonNull(command, "The Command must not be null")),
            arguments == null ? Stream.empty() : Arrays.stream(arguments)));
    }
}
