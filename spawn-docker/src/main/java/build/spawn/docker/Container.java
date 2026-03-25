package build.spawn.docker;

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

import build.base.configuration.Configuration;
import build.base.configuration.Option;
import build.base.io.Terminal;
import build.spawn.docker.option.Command;
import build.spawn.docker.option.ExposedPort;
import build.spawn.docker.option.Link;

import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Represents a {@code Container} created from an {@link Image}.
 *
 * @author brian.oliver
 * @since Jun-2021
 */
public interface Container
    extends AutoCloseable {

    /**
     * Obtains the {@link Image} from which the {@link Container} was created.
     *
     * @return the {@link Image}
     */
    Image image();

    /**
     * Obtains the identity of the {@link Container}.
     *
     * @return the identity of the {@link Container}
     */
    String id();

    /**
     * Obtains the {@link Configuration} used to create the {@link Container}.
     *
     * @return the {@link Configuration}
     */
    Configuration configuration();

    /**
     * Obtains a {@link CompletableFuture} that will be completed when the {@link Container} has completed starting.
     *
     * @return a {@link CompletableFuture} with the {@link Container} when it has started
     */
    CompletableFuture<Container> onStart();

    /**
     * Obtains a {@link CompletableFuture} that will be completed when the {@link Container} has been terminated,
     * either naturally, gracefully though {@link #stop()}ping, ungracefully through {@link #kill()}ing, or otherwise
     * and thus exited.
     *
     * @return a {@link CompletableFuture} with the {@link Container} when it has exited
     */
    CompletableFuture<Container> onExit();

    /**
     * Obtains the optionally available exit value for a {@link Container}.
     * <p>
     * Upon termination, a {@link Container} may provide an exit value.  Typically a value of {@code 0} indicates normal
     * termination.  However, in some circumstances it may not be possible to determine any such value, in which case
     * the exit value returned will not be present.
     * <p>
     * This method should not be used to test for or trigger operations based on {@link Container} termination.  A
     * returned value of {@link Optional#empty()} does not indicate termination, either successfully or otherwise,
     * just that the exit value is not available.  Instead {@link #onExit()} should be used for these purposes.
     *
     * @return the {@link Optional} exit value for a {@link Container}
     */
    OptionalInt exitValue();

    /**
     * Attempts to attach a {@link Terminal} to the {@link Container}, either while it's starting, after it has
     * started or after it has terminated.
     * <p>
     * Attempting to attach a {@link Terminal} to a {@link Container} that has not yet started or not yet completed
     * starting is permitted.  However, reading will inevitably block the {@link Terminal} from responding and
     * writing to the {@link Terminal} will be buffered.
     * <p>
     * Attempting to attach a {@link Terminal} to a terminated {@link Container} may be permitted when the underlying
     * {@link Container} is still available.  For example, it has not been removed (pruned) by the
     * {@code Docker Engine}.  When attaching to a terminated {@link Container}, reading may be permitted, however
     * writing to the {@link Terminal} will be ignored.
     * <p>
     * As it's possible to attach to a terminated {@link Terminal}, one should not assume the termination of
     * either the {@link java.io.Reader} and/or {@link java.io.Writer} streams provided by a {@link Terminal}s indicate
     * the state of a {@link Container}.
     *
     * @param configuration the {@link Configuration} for attaching to the {@link Container}
     * @return the {@link Terminal} that was attached
     */
    Terminal attach(Configuration configuration);

    /**
     * Attempts to attach a {@link Terminal} to the {@link Container}, either while it's starting, after it has
     * started or after it has terminated.
     * <p>
     * Attempting to attach a {@link Terminal} to a {@link Container} that has not yet started or not yet completed
     * starting is permitted.  However, reading will inevitably block the {@link Terminal} from responding and
     * writing to the {@link Terminal} will be buffered.
     * <p>
     * Attempting to attach a {@link Terminal} to a terminated {@link Container} may permitted when the underlying
     * {@link Container} is still available.  For example, it has not been removed (pruned) by the
     * {@code Docker Engine}.  When attaching to a terminated {@link Container}, reading may be permitted, however
     * writing to the {@link Terminal} will be ignored.
     * <p>
     * As it's possible to attach to a terminated {@link Terminal}, one should not assume the termination of
     * either the {@link java.io.Reader} and/or {@link java.io.Writer} streams provided by a {@link Terminal}s indicate
     * the state of a {@link Container}.
     *
     * @param options the {@link Option}s for attaching to the {@link Container}
     * @return the {@link Terminal} that was attached
     */
    default Terminal attach(final Option... options) {
        return attach(Configuration.of(options));
    }

    /**
     * Attempts to attach a {@link Terminal} the {@link Container}.
     *
     * @return a {@link CompletableFuture} with the {@link Terminal} that was attached
     * @see #attach(Option...)
     */
    default Terminal attach() {
        return attach(Configuration.empty());
    }

    /**
     * Creates an {@link Executable} representing a {@link Command} to be executed in the {@link Container}.
     *
     * @param command the {@link Command}
     * @return the {@link Executable}
     */
    Executable createExecutable(Command command);

    /**
     * Creates an {@link Executable} representing the specified command and provided arguments to be executed in
     * the {@link Container}.
     *
     * @param command   the command
     * @param arguments the arguments
     * @return the {@link Executable}
     */
    default Executable createExecutable(final String command, final String... arguments) {
        return createExecutable(Command.of(command, arguments));
    }

    /**
     * Attempts to remove the {@link Container} after it has terminated.
     * <p>
     * Should the {@link Container} not be terminated, an attempt will be made to {@link #kill(Option... options)}
     * the {@link Container} first.
     *
     * @param configuration the {@link Configuration}s for stopping and deleting the {@link Container}
     */
    void remove(Configuration configuration);

    /**
     * Attempts to remove the {@link Container} after it has terminated.
     * <p>
     * Should the {@link Container} not be terminated, an attempt will be made to {@link #kill(Option... options)}
     * the {@link Container} first.
     *
     * @param options the {@link Option}s for stopping and deleting the {@link Container}
     */
    default void remove(final Option... options) {
        remove(Configuration.of(options));
    }

    /**
     * Attempts to delete the {@link Container} after it has terminated.
     * <p>
     * Should the {@link Container} not be terminated, an attempt will be made to {@link #kill()}
     * the {@link Container} first.
     */
    default void remove() {
        remove(Configuration.empty());
    }

    /**
     * Creates a {@code TAR} from one or more files and copies the archive from the local file system to a directory
     * inside the container and extracts the archive in the destination directory.
     *
     * @param archivePath          the path on the local directory where to create the {@code TAR}
     * @param destinationDirectory directory inside the container to copy and extract contents
     * @param filesToCopy          one or more files to copy
     */
    void copyFiles(Path archivePath, String destinationDirectory, Path... filesToCopy);

    /**
     * Gets information about a file in the container.
     *
     * @param filePath the path of the file in the container
     * @return an optional {@link Map} containing information from the {@code x-docker-container-path-stat} response header
     */
    Optional<Map<String, String>> fileInformation(Path filePath);

    /**
     * Attempts to gracefully stop the {@link Container}.
     * <p>
     * Should the {@link Container} not exist or already be stopped the request is ignored.
     *
     * @param configuration the {@link Configuration} for stopping the {@link Container}
     */
    void stop(Configuration configuration);

    /**
     * Attempts to gracefully stop the {@link Container}.
     * <p>
     * Should the {@link Container} not exist or already be stopped the request is ignored.
     *
     * @param options the {@link Option}s for stopping the {@link Container}
     */
    default void stop(final Option... options) {
        stop(Configuration.of(options));
    }

    /**
     * Attempts to gracefully stop the {@link Container}.
     * <p>
     * Should the {@link Container} not exist or already be stopped the request is ignored.
     */
    default void stop() {
        stop(Configuration.empty());
    }

    /**
     * Attempts to kill the {@link Container}.
     * <p>
     * Should the {@link Container} not exist or already be stopped the request is ignored.
     *
     * @param configuration the {@link Configuration} for killing the {@link Container}
     */
    void kill(Configuration configuration);

    /**
     * Attempts to kill the {@link Container}.
     * <p>
     * Should the {@link Container} not exist or already be stopped the request is ignored.
     *
     * @param options the {@link Option}s for killing the {@link Container}
     */
    default void kill(final Option... options) {
        kill(Configuration.of(options));
    }

    /**
     * Attempts to kill the {@link Container}.
     * <p>
     * Should the {@link Container} not exist or already be stopped the request is ignored.
     */
    default void kill() {
        kill(Configuration.empty());
    }

    /**
     * Requests the {@link Container} to be paused.
     * <p>
     * If the {@link Container} is not alive, the returned {@link CompletableFuture} is completed exceptionally.
     *
     * @return {@link CompletableFuture} indicating when the {@link Container} was suspended
     */
    CompletableFuture<Container> pause();

    /**
     * Requests that a previously {@link #pause() paused} {@link Container} be unpaused (resumed).
     * <p>
     * If the {@link Container} is not alive, the returned {@link CompletableFuture} is completed exceptionally.
     *
     * @return {@link CompletableFuture} indicating when the {@link Container} was resumed
     */
    CompletableFuture<Container> unpause();

    /**
     * Inspects a {@link Container} returning a snapshot of the currently available {@link Information}.  Should
     * no {@link Information} be available, an {@link Optional#empty()} is returned.
     *
     * @return {@link Optional} {@link Information}
     */
    Optional<Information> inspect();

    @Override
    default void close() {
        kill();
    }

    /**
     * The state of a {@link Container}.
     */
    enum State {
        /**
         * Indicates that the container is created.
         */
        CREATED,

        /**
         * Indicates that the container is restarting.
         */
        RESTARTING,

        /**
         * Indicates that the container is running.
         */
        RUNNING,

        /**
         * Indicates that the container is being removed.
         */
        REMOVING,

        /**
         * Indicates that the container is paused.
         */
        PAUSED,

        /**
         * Indicates that the container has exited.
         */
        EXITED,

        /**
         * Indicates that the container is dead.
         */
        DEAD
    }

    /**
     * Represents information concerning the current state of a {@link Container}.
     */
    interface Information {

        /**
         * Obtains an {@link InetSocketAddress} reachable from the calling context.
         * <p>
         * The published port of a running container may be on a network interface not reachable
         * by the calling context (e.g. when the calling context is a sibling container).  This method
         * attempts to detect such an environment and return an endpoint on an interface suitable for the
         * calling context.
         *
         * @param publishedPort the {@link PublishedPort} exposed by the {@link Container}.
         * @return a new {@link InetSocketAddress} reachable from the calling context.
         */
        static Optional<InetSocketAddress> getLocalEndpoint(final PublishedPort publishedPort) {
            // Of the many ways to detect if we are inside a container, looking for the presence of '/.dockerenv'
            // is the simplest and meets our required use case.
            return publishedPort.addresses()
                .findFirst()
                .map(pubAddr -> Files.exists(Paths.get("/.dockerenv"))
                    ? new InetSocketAddress("host.docker.internal", pubAddr.getPort())
                    : new InetSocketAddress("localhost", pubAddr.getPort()));
        }

        /**
         * Obtains the identity of the {@link Container}.
         *
         * @return the identity of the {@link Container}
         */
        String containerId();

        /**
         * Obtains the name of the {@link Container}.
         *
         * @return the name of the {@link Container}
         */
        String name();

        /**
         * Obtains a {@link Stream} of the {@link ExposedPort}s for the {@link Container}.
         *
         * @return a {@link Stream} of the {@link ExposedPort}s
         */
        Stream<ExposedPort> exposedPorts();

        /**
         * Obtains a {@link Stream} of the {@link PublishedPort}s for the {@link Container}.
         *
         * @return a {@link Stream} of the {@link PublishedPort}s
         */
        Stream<PublishedPort> publishedPorts();

        /**
         * Obtains the native process id for the main process of the {@link Container}.
         *
         * @return the native process id
         */
        long pid();

        /**
         * Obtains the {@link Optional} {@link State} of the {@link Container}, or {@link Optional#empty()} if unknown.
         *
         * @return the {@link Optional} {@link State} of the {@link Container}
         */
        Optional<State> state();

        /**
         * Obtains a {@link Stream} of links for the container.
         *
         * @return a {@link Stream} of links for the container
         */
        Stream<Link> links();

        /**
         * Gets the {@code IP} address of a container.
         *
         * @return the {@code IP} address
         */
        String ipAddress();
    }
}
