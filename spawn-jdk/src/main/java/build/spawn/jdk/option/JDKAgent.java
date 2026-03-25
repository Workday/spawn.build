package build.spawn.jdk.option;

/*-
 * #%L
 * Spawn JDK
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

import build.base.configuration.CollectedOption;
import build.base.configuration.ConfigurationBuilder;
import build.base.configuration.Option;
import build.spawn.application.Platform;
import build.spawn.jdk.JDKApplication;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * An immutable {@link Option} to define an Agent for a {@link JDKApplication}.
 *
 * @author brian.oliver
 * @since Oct-2018
 */
public class JDKAgent
    implements CollectedOption<List>, JDKOption {

    /**
     * The path to the agent jar.
     */
    private final Path path;

    /**
     * The {@link Optional} arguments for the agent.
     */
    private final Optional<String> arguments;

    /**
     * Constructs a {@link JDKAgent}.
     *
     * @param path      the {@link Path} to the agent jar
     * @param arguments the {@link Optional} arguments
     */
    private JDKAgent(final Path path, final Optional<String> arguments) {
        this.path = path;
        this.arguments = arguments == null ? Optional.empty() : arguments;
    }

    /**
     * Obtains the {@link Path} to the {@link JDKAgent} jar.
     *
     * @return the {@link Path} to the {@link JDKAgent} jar
     */
    public Path path() {
        return this.path;
    }

    /**
     * Obtains the {@link Optional} arguments for the {@link JDKAgent}.
     *
     * @return the {@link Optional} arguments for the {@link JDKAgent}
     */
    public Optional<String> arguments() {
        return this.arguments;
    }

    @Override
    public Stream<String> resolve(final Platform platform,
                                  final ConfigurationBuilder options) {

        return Stream.of("-javaagent:" + this.path + (this.arguments.map(s -> "=" + s).orElse("")));
    }

    /**
     * Creates a {@link JDKAgent}.
     *
     * @param path the path for the {@link JDKAgent} jar
     * @return a {@link JDKAgent}
     */
    public static JDKAgent of(final String path) {
        return of(Paths.get(path));
    }

    /**
     * Creates a {@link JDKAgent}.
     *
     * @param path      the path for the {@link JDKAgent} jar
     * @param arguments the {@link Optional} (nullable) arguments for the {@link JDKAgent}
     * @return a {@link JDKAgent}
     */
    public static JDKAgent of(final String path, final String arguments) {
        return of(Paths.get(path), arguments);
    }

    /**
     * Creates a {@link JDKAgent}.
     *
     * @param path the path for the {@link JDKAgent} jar
     * @return a {@link JDKAgent}
     */
    public static JDKAgent of(final Path path) {
        return new JDKAgent(path, Optional.empty());
    }

    /**
     * Creates a {@link JDKAgent}.
     *
     * @param path      the path for the {@link JDKAgent} jar
     * @param arguments the {@link Optional} (nullable) arguments for the {@link JDKAgent}
     * @return a {@link JDKAgent}
     */
    public static JDKAgent of(final Path path, final String arguments) {
        return new JDKAgent(path, Optional.ofNullable(arguments));
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        else if (object == null || getClass() != object.getClass()) {
            return false;
        }
        else {
            final JDKAgent other = (JDKAgent) object;
            return Objects.equals(this.path, other.path) && Objects.equals(this.arguments, other.arguments);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.path, this.arguments);
    }
}
