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

import build.base.foundation.Strings;
import build.spawn.docker.option.ExposedPort;
import build.spawn.docker.option.ImageName;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides the ability to build {@code Dockerfile}s for {@code Docker}, according to
 * <a href="https://docs.docker.com/engine/reference/builder/">Dockerfile Reference</a>.
 * <p>
 * With exception to the {@link #build(Path)}, {@link #build(Writer)} and {@link #build()} methods, each invocation
 * of a method in this class will add an instruction to the {@code Dockerfile} when it is built, in the order of
 * innovation.
 * <p>
 * This class is typically used with the {@link DockerContextBuilder} in order to build custom {@code Docker}
 * {@link Image}s.
 *
 * @author brian.oliver
 * @see DockerContextBuilder
 * @see Images
 * @since Aug-2021
 */
public class DockerFileBuilder {

    /**
     * The content to write to the {@code Dockerfile}.
     */
    private final LinkedList<String> content;

    /**
     * Constructs a new {@link DockerFileBuilder}.
     */
    public DockerFileBuilder() {
        this.content = new LinkedList<>();
    }

    /**
     * Adds a <a href="https://docs.docker.com/engine/reference/builder/#from">FROM</a> declaration to the
     * {@link DockerFileBuilder}.
     *
     * @param nameOrId the {@link Image} name
     * @return this {@link DockerFileBuilder} to permit fluent-style method invocation
     */
    public DockerFileBuilder from(final String nameOrId) {

        if (!Strings.isEmpty(nameOrId)) {
            this.content.add("FROM " + nameOrId);
        }
        return this;
    }

    /**
     * Adds a <a href="https://docs.docker.com/engine/reference/builder/#from">FROM</a> declaration to the
     * {@link DockerFileBuilder}.
     *
     * @param imageName {@link ImageName}
     * @return this {@link DockerFileBuilder} to permit fluent-style method invocation
     */
    public DockerFileBuilder from(final ImageName imageName) {

        if (imageName != null) {
            return from(imageName.get());
        }
        return this;
    }

    /**
     * Adds a <a href="https://docs.docker.com/engine/reference/builder/#workdir">WORKDIR</a> declaration to the
     * {@link DockerFileBuilder}.
     *
     * @param directory the directory
     * @return this {@link DockerFileBuilder} to permit fluent-style method invocation
     */
    public DockerFileBuilder workingDirectory(final String directory) {

        if (!Strings.isEmpty(directory)) {
            this.content.add("WORKDIR " + Strings.doubleQuoteIfContainsWhiteSpace(directory));
        }

        return this;
    }

    /**
     * Adds a <a href="https://docs.docker.com/engine/reference/builder/#workdir">WORKDIR</a> declaration to the
     * {@link DockerFileBuilder}.
     *
     * @param directory the {@link Path} of a directory
     * @return this {@link DockerFileBuilder} to permit fluent-style method invocation
     */
    public DockerFileBuilder workingDirectory(final Path directory) {

        return directory == null
            ? this
            : workingDirectory(directory.toString());
    }

    /**
     * Adds a <a href="https://docs.docker.com/engine/reference/builder/#cmd">CMD</a> declaration to the
     * {@link DockerFileBuilder}.
     *
     * @param command    the {@link String} command
     * @param parameters the optional {@link String} parameters from the command
     * @return this {@link DockerFileBuilder} to permit fluent-style method invocation
     */
    public DockerFileBuilder command(final String command, final String... parameters) {

        return command(command, parameters == null ? Stream.empty() : Arrays.stream(parameters));
    }

    /**
     * Adds a <a href="https://docs.docker.com/engine/reference/builder/#cmd">CMD</a> declaration to the
     * {@link DockerFileBuilder}.
     *
     * @param command    the {@link String} command
     * @param parameters the {@link String} parameters from the command
     * @return this {@link DockerFileBuilder} to permit fluent-style method invocation
     */
    public DockerFileBuilder command(final String command, final Stream<String> parameters) {

        if (Strings.isEmpty(command)) {
            return this;
        }

        // collect the parameters into a comma separated, double-quoted list
        final String collected = parameters == null ? ""
            : parameters
                .map(parameter -> Strings.isEmpty(parameter) ? "" : parameter)
                .map(parameter -> "\"" + parameter + "\"")
                .collect(Collectors.joining(", "));

        this.content.add("CMD ["
            + "\"" + command + "\""
            + (Strings.isEmpty(collected) ? "" : ", " + collected)
            + "]");
        return this;
    }

    /**
     * Adds one or more <a href="https://docs.docker.com/engine/reference/builder/#expose">EXPOSE</a> declarations
     * to the {@link DockerFileBuilder}.
     *
     * @param exposedPorts the {@link ExposedPort}s
     * @return this {@link DockerFileBuilder} to permit fluent-style method invocation
     */
    public DockerFileBuilder expose(final ExposedPort... exposedPorts) {

        if (exposedPorts != null && exposedPorts.length > 0) {
            Arrays.stream(exposedPorts)
                .forEach(exposedPort -> this.content.add(
                    "EXPOSE " + exposedPort.port() + "/" + exposedPort.type().name().toLowerCase()));
        }
        return this;
    }

    /**
     * Adds a <a href="https://docs.docker.com/engine/reference/builder/#copy">COPY</a> declaration to the
     * {@link DockerFileBuilder}.
     *
     * @param destination the destination {@link Path} (in the associated {@code DockerContext})
     * @param sources     the source {@link Path} (in the {@link Image})
     * @return this {@link DockerFileBuilder} to permit fluent-style method invocation
     */
    public DockerFileBuilder copyInto(final Path destination,
                                      final Path... sources) {

        if (destination == null || sources == null || sources.length == 0) {
            return this;
        }

        this.content.add("COPY ["
            + Arrays.stream(sources)
            .map(Path::toString)
            .map(path -> "\"" + path + "\"")
            .collect(Collectors.joining(", "))
            + ", \""
            + destination
            + "\"]");
        return this;
    }

    /**
     * Adds a <a href="https://docs.docker.com/engine/reference/builder/#copy">COPY</a> declaration to the
     * {@link DockerFileBuilder}.
     *
     * @param destination the destination path (in the associated {@code DockerContext})
     * @param sources     the source path (in the {@link Image})
     * @return this {@link DockerFileBuilder} to permit fluent-style method invocation
     */
    public DockerFileBuilder copyInto(final String destination,
                                      final String... sources) {

        if (Strings.isEmpty(destination) || sources == null || sources.length == 0) {
            return this;
        }

        return copyInto(
            Paths.get(destination),
            Arrays.stream(sources)
                .map(Paths::get)
                .toArray(Path[]::new));
    }

    /**
     * Creates the {@code Dockerfile} in a temporary path
     *
     * @return the {@link Path} of the {@code Dockerfile} that was created
     * @throws IOException should creating the {@link DockerFileBuilder} fail
     */
    public Path build()
        throws IOException {

        return build(Files.createTempDirectory("quark-docker-"));
    }

    /**
     * Creates the {@code Dockerfile} in the specified directory {@link Path}.
     *
     * @param path the {@link Path} in which to create the {@code Dockerfile}
     * @return the {@link Path} of the {@code Dockfile} that was created
     * @throws IOException should creating the {@link DockerFileBuilder} fail
     */
    public Path build(final Path path)
        throws IOException {

        Objects.requireNonNull(path, "The Path must not be null");

        if (!Files.isDirectory(path) || !Files.exists(path)) {
            throw new IOException("The [" + path.toString() + "] is not a directory that exists");
        }

        final Path dockerFilePath = path.resolve("Dockerfile");

        final Writer writer = Files.newBufferedWriter(dockerFilePath);
        build(writer);
        writer.close();

        return dockerFilePath;
    }

    /**
     * Creates the {@code Dockerfile} in the {@link Writer}.
     *
     * @param writer the {@link Writer}
     * @throws IOException should writing to the {@link Writer} fail
     */
    public void build(final Writer writer)
        throws IOException {

        Objects.requireNonNull(writer, "The PrintWriter must not be null");

        final PrintWriter printWriter = new PrintWriter(writer);
        this.content.forEach(printWriter::println);
    }
}
