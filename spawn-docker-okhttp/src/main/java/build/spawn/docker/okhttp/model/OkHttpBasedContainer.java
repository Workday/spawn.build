package build.spawn.docker.okhttp.model;

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
import build.base.configuration.ConfigurationBuilder;
import build.base.configuration.Option;
import build.base.flow.CompletingSubscriber;
import build.base.flow.FilteringSubscriber;
import build.base.flow.MappingSubscriber;
import build.base.flow.Publisher;
import build.base.io.Terminal;
import build.codemodel.injection.Context;
import build.codemodel.injection.PostInject;
import build.spawn.docker.Container;
import build.spawn.docker.Event;
import build.spawn.docker.Executable;
import build.spawn.docker.Execution;
import build.spawn.docker.Image;
import build.spawn.docker.okhttp.command.AttachContainer;
import build.spawn.docker.okhttp.command.CopyFiles;
import build.spawn.docker.okhttp.command.CreateExecution;
import build.spawn.docker.okhttp.command.DeleteContainer;
import build.spawn.docker.okhttp.command.FileInformation;
import build.spawn.docker.okhttp.command.InspectContainer;
import build.spawn.docker.okhttp.command.KillContainer;
import build.spawn.docker.okhttp.command.PauseContainer;
import build.spawn.docker.okhttp.command.StartExecution;
import build.spawn.docker.okhttp.command.StopContainer;
import build.spawn.docker.okhttp.command.UnpauseContainer;
import build.spawn.docker.okhttp.event.StatusEvent;
import build.spawn.docker.option.Command;
import jakarta.inject.Inject;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;

/**
 * An internal implementation of a {@link Container}.
 *
 * @author brian.oliver
 * @since Jun-2021
 */
public class OkHttpBasedContainer
    extends AbstractJsonBasedResult
    implements Container {

    /**
     * The {@link Image} from which the {@link Container} was created.
     */
    @Inject
    private Image image;

    /**
     * The {@link Context} used to create the {@link Container}.
     */
    @Inject
    private Context context;

    /**
     * The {@link Publisher} of {@link Event}s for the {@link Container}.
     */
    @Inject
    private Publisher<Event> publisher;

    /**
     * The {@link Configuration} for the {@link Container}.
     */
    @Inject
    private Configuration configuration;

    /**
     * The identity for the {@link Container}.
     */
    private String id;

    /**
     * The {@link CompletingSubscriber} to allow observation of specific {@code Container} {@link StatusEvent}s.
     */
    private CompletingSubscriber<StatusEvent> completingSubscriber;

    /**
     * The {@link CompletableFuture} indicating when the {@link Container} has started.
     */
    private CompletableFuture<Container> onStart;

    /**
     * The {@link CompletableFuture} indicating when the {@link Container} has exited (died) and won't be restarted.
     */
    private CompletableFuture<Container> onExit;

    /**
     * The {@link Integer} exit code for the {@link Container}.
     * <p>
     * This will be {@code null} when there's no exit value available.
     */
    private volatile Integer exitValue;

    /**
     * Perform post-injection initialization.
     */
    @PostInject
    private void onPostInject() {

        // introduce the Container to the Context
        // (so that it may be injected into anything, like Commands, that use this Context)
        this.context.bind(Container.class).to(this);

        // obtain the identity for the Container from the JsonNode
        this.id = jsonNode().get("Id").asText();

        System.out.println("Created Container: " + this.id.substring(this.id.length() - 8));

        // establish a CompletingSubscriber for the Container
        this.completingSubscriber = new CompletingSubscriber<>();
        this.publisher.subscribe(
            FilteringSubscriber.of(StatusEvent.class::isInstance,
                MappingSubscriber.of(StatusEvent.class::cast,
                    FilteringSubscriber.of(
                        event -> event.jsonNode().get("id").asText().equals(this.id),
                        this.completingSubscriber))));

        // establish the CompletableFuture to identify when the Container has started
        this.onStart = this.completingSubscriber.when(
            event -> "start".equals(event.jsonNode().get("Action").asText()),
            __ -> {
                System.out.println("Started Container: " + this.id.substring(this.id.length() - 8));
                return this;
            });

        // establish the CompletableFuture to identify when the Container has terminated (died)
        this.onExit = this.completingSubscriber.when(event -> {
                final var jsonNode = event.jsonNode();
                final var action = jsonNode.get("Action").asText();

                if ("die".equals(action)) {
                    // extract the exitCode as the exitValue for the Container
                    final var exitCode = jsonNode.get("Actor").get("Attributes").get("exitCode");
                    this.exitValue = exitCode.isMissingNode() ? null : exitCode.asInt();

                    return true;
                }

                return false;
            },
            _ -> {
                System.out.println("Exited Container: " + this.id.substring(this.id.length() - 8));

                return this;
            });

        this.exitValue = null;
    }

    /**
     * Creates a new dependency injection {@link Context}, based on the {@link Context} used to
     * create the {@link Container}.
     *
     * @return a new {@link Context}
     */
    protected Context createContext() {
        return this.context.newContext();
    }

    @Override
    public Image image() {
        return this.image;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public Configuration configuration() {
        return this.configuration;
    }

    @Override
    public CompletableFuture<Container> onStart() {
        return this.onStart;
    }

    @Override
    public CompletableFuture<Container> onExit() {
        return this.onExit;
    }

    @Override
    public OptionalInt exitValue() {
        final Integer value = this.exitValue;
        return value == null ? OptionalInt.empty() : OptionalInt.of(value);
    }

    @Override
    public Terminal attach(final Configuration configuration) {
        return createContext()
            .inject(new AttachContainer(configuration))
            .submit();
    }

    @Override
    public Executable createExecutable(final Command command) {
        // establish the ConfigurationBuilder for the Execution of our command
        final var options = ConfigurationBuilder.create()
            .add(command);

        // establish a new Execution.Builder for our command
        return new Executable() {

            /**
             * By default, the {@link Terminal} for the {@link Execution} isn't required
             */
            boolean terminalRequired = false;

            @Override
            public Executable with(final Option option) {
                options.add(option);
                return this;
            }

            @Override
            public Executable withTerminal(final boolean enabled) {
                this.terminalRequired = enabled;
                return this;
            }

            @Override
            public Execution execute() {
                final var configuration = options.build();

                // attempt to create a Docker exec instance
                final var id = createContext()
                    .inject(new CreateExecution(OkHttpBasedContainer.this, terminalRequired, configuration))
                    .submit();

                // attempt to start the exec instance
                return createContext()
                    .inject(new StartExecution(OkHttpBasedContainer.this, id, terminalRequired, configuration))
                    .submit();
            }
        };
    }

    @Override
    public void remove(final Configuration configuration) {
        kill(configuration);

        createContext()
            .inject(new DeleteContainer(configuration))
            .submit();
    }

    @Override
    public void copyFiles(final Path archivePath, final String destinationDirectory, final Path... filesToCopy) {
        createContext()
            .inject(new CopyFiles(archivePath, destinationDirectory, filesToCopy))
            .submit();
    }

    @Override
    public Optional<Map<String, String>> fileInformation(final Path filePath) {
        return createContext()
            .inject(new FileInformation(filePath))
            .submit();
    }

    @Override
    public void stop(final Configuration configuration) {
        createContext()
            .inject(new StopContainer(configuration))
            .submit();
    }

    @Override
    public void kill(final Configuration configuration) {
        createContext()
            .inject(new KillContainer(configuration))
            .submit();
    }

    @Override
    public CompletableFuture<Container> pause() {
        // establish a CompletableFuture to notify when the Container has been paused
        final CompletableFuture<Container> future = this.completingSubscriber
            .when(event -> "pause".equals(event.jsonNode().get("Action").asText()), __ -> this);

        createContext()
            .create(PauseContainer.class)
            .submit();

        return future;
    }

    @Override
    public CompletableFuture<Container> unpause() {
        // establish a CompletableFuture to notify when the Container has been unpaused
        final CompletableFuture<Container> future = this.completingSubscriber
            .when(event -> "unpause".equals(event.jsonNode().get("Action").asText()), __ -> this);

        createContext()
            .create(UnpauseContainer.class)
            .submit();

        return future;
    }

    @Override
    public Optional<Information> inspect() {
        return createContext()
            .inject(new InspectContainer(this))
            .submit();
    }

    @Override
    public void close() {
        Container.super.close();

        // we no longer want to receive any more events for the Container
        this.completingSubscriber.cancel();
    }
}
