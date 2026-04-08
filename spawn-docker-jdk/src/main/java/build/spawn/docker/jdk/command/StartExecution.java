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
import build.base.io.NullWriter;
import build.base.io.Terminal;
import build.spawn.docker.Container;
import build.spawn.docker.Execution;
import build.spawn.docker.jdk.HttpTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;

import java.io.IOException;
import java.io.PipedReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * The {@code Docker Daemon} {@link Command} to start an {@link Execution} using the
 * <a href="https://docs.docker.com/engine/api/v1.41/#operation/ExecStart">Start Execution</a> command.
 *
 * @author brian.oliver
 * @since Sep-2021
 */
public class StartExecution
    extends AbstractNonBlockingCommand<Execution> {

    /**
     * The {@link ObjectMapper} for parsing json.
     */
    @Inject
    private ObjectMapper objectMapper;

    /**
     * The {@link Container} to start.
     */
    private final Container container;

    /**
     * The identity of the {@link Execution}.
     */
    private final String id;

    /**
     * Is a {@link Terminal} required for the {@link Execution}.
     */
    private final boolean terminalRequired;

    /**
     * The {@link Configuration} required for the {@link Execution}.
     */
    private final Configuration configuration;

    /**
     * Constructs a {@link StartExecution} {@link Command}.
     *
     * @param container        the {@link Container} in which the {@link Execution} is being started
     * @param id               the identity of the {@link Execution}
     * @param terminalRequired is a {@link Terminal} required for the {@link Execution}
     * @param configuration    the {@link Configuration} used to create the {@link Execution}
     */
    public StartExecution(final Container container,
                          final String id,
                          final boolean terminalRequired,
                          final Configuration configuration) {

        this.container = Objects.requireNonNull(container, "The Container must not be null");
        this.id = Objects.requireNonNull(id, "The identity of the Execution must not be null");
        this.configuration = Objects.requireNonNull(configuration, "The OptionsByType must not be null");

        this.terminalRequired = terminalRequired;
    }

    @Override
    protected HttpTransport.Request createRequest() {

        // establish an ObjectNode containing the containers/create json
        final var node = this.objectMapper.createObjectNode();
        node.put("Detach", !this.terminalRequired);
        node.put("Tty", false);

        return HttpTransport.Request
            .post("/exec/" + this.id + "/start", node.toString().getBytes(StandardCharsets.UTF_8))
            .withContentType("application/json");
    }

    @Override
    protected Execution createResult(final HttpTransport.Response response)
        throws IOException {

        // establish PipedReaders for stdout and stderr
        final PipedReader outputReader = new PipedReader();
        final PipedReader errorReader = new PipedReader();

        // TODO: establish PipedWriter for stdin

        // establish the Terminal
        final var terminal = new Terminal() {
            @Override
            public Reader getOutputReader() {
                return outputReader;
            }

            @Override
            public Reader getErrorReader() {
                return errorReader;
            }

            @Override
            public Writer getInputWriter() {
                return NullWriter.get();
            }

            @Override
            public void close() {
                // closing the Terminal has no effect on execution
            }

            @Override
            public CompletableFuture<?> onClosed() {
                return StartExecution.this.processing();
            }
        };

        // establish a FrameProcessor to redirect the I/O frames to the Terminal
        final var processor = new FrameProcessor(response.bodyStream(), outputReader, errorReader);

        processor.processing()
            .whenComplete((_, error) -> {
                if (error == null) {
                    onProcessingCompletion();
                } else {
                    onProcessingError(error);
                }
            });

        final var name = "container-" + this.container.id()
            .substring(this.container.id().length() - 8) + "-exec-" + this.id;

        Thread.ofVirtual()
            .name(name)
            .start(processor);

        // create the execution
        return new Execution() {
            @Override
            public Container container() {
                return StartExecution.this.container;
            }

            @Override
            public String id() {
                return StartExecution.this.id;
            }

            @Override
            public Configuration configuration() {
                return StartExecution.this.configuration;
            }

            @Override
            public Optional<Terminal> terminal() {
                return StartExecution.this.terminalRequired
                    ? Optional.of(terminal)
                    : Optional.empty();
            }

            @Override
            public Optional<Information> inspect() {
                return StartExecution.this.createContext()
                    .inject(new InspectExecution(StartExecution.this.id))
                    .submit();
            }

            @Override
            public CompletableFuture<Execution> onExit() {
                return StartExecution.this.processing()
                    .thenApply(__ -> this);
            }
        };
    }
}
