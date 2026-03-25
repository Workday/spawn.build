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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * Provides access to the {@link Image}s API for a {@link Session}.
 *
 * @author brian.oliver
 * @since Jun-2021
 */
public interface Images {

    /**
     * Attempts to obtain an {@link Image} that is available to the {@code Docker Engine}.
     *
     * @param nameOrId the name
     * @return an {@link Optional} for {@link Image}
     */
    Optional<Image> get(String nameOrId);

    /**
     * Attempts to obtain an {@link Image} that is available to the {@code Docker Engine}.
     *
     * @param nameOrId      the name
     * @param configuration additional {@link Configuration}
     * @return an {@link Optional} for {@link Image}
     */
    Optional<Image> get(String nameOrId,
                        Configuration configuration);

    /**
     * Attempts to pull an {@link Image} from the configured {@code Docker Repository} for use by the
     * {@code Docker Engine}.
     *
     * @param nameOrId      the name
     * @param configuration additional {@link Configuration}s
     * @return a {@link Optional} the {@link Image} that was successfully pulled
     */
    Optional<Image> pull(String nameOrId,
                         Configuration configuration);

    /**
     * Attempts to pull an {@link Image} from the configured {@code Docker Repository} for use by the
     * {@code Docker Engine}.
     *
     * @param nameOrId the name
     * @return a {@link Optional} the {@link Image} that was successfully pulled
     */
    Optional<Image> pull(String nameOrId);

    /**
     * Builds a new {@link Image} based on the information defined in the {@code Docker Context} {@link Path}.
     * <p>
     * Currently, the {@code Docker Context} {@link Path} must be a {@link Path} to a {@code tarball} that can be used
     * by the {@code Docker Engine} to build the {@link Image}.
     * <p>
     * The returned {@link Image} can be optionally named and tagged once it is built by providing one or more
     * {@link build.spawn.docker.option.ImageName}s.
     *
     * @param contextPath   the {@link Path} of the {@code Docker Context} from which to create the {@link Image}
     * @param configuration {@link Configuration} for creating the {@link Image}
     * @return the {@link Optional} {@link Image}
     * @see build.spawn.docker.option.ImageName
     */
    Optional<Image> build(Path contextPath,
                          Configuration configuration);

    /**
     * Builds a new {@link Image} based on the information defined in the {@code Docker Context} {@link Path}.
     * <p>
     * Currently, the {@code Docker Context} {@link Path} must be a {@link Path} to a {@code tarball} that can be used
     * by the {@code Docker Engine} to build the {@link Image}.
     * <p>
     * The returned {@link Image} can be optionally named and tagged once it is built by providing one or more
     * {@link build.spawn.docker.option.ImageName}s.
     *
     * @param contextPath the {@link Path} of the {@code Docker Context} from which to create the {@link Image}
     * @param options     {@link Option}s for creating the {@link Image}
     * @return the {@link Optional} {@link Image}
     * @see build.spawn.docker.option.ImageName
     */
    default Optional<Image> build(final Path contextPath,
                                  final Option... options) {

        return build(contextPath, Configuration.of(options));
    }

    /**
     * Builds a new {@link Image} based on the {@code Docker Context} created by the provided
     * {@link DockerContextBuilder}.
     * <p>
     * Currently, the {@code Docker Context} {@link Path} must be a {@link Path} to a {@code tarball} that can be used
     * by the {@code Docker Engine} to build the {@link Image}.
     * <p>
     * The returned {@link Image} can be optionally named and tagged once it is built by providing one or more
     * {@link build.spawn.docker.option.ImageName}s.
     *
     * @param builder the {@link DockerContextBuilder} for the {@code Docker Context}
     * @param options {@link Option}s for creating the {@link Image}
     * @return the {@link Optional} {@link Image}
     * @see build.spawn.docker.option.ImageName
     */
    default Optional<Image> build(final DockerContextBuilder builder,
                                  final Option... options)
        throws IOException {

        Objects.requireNonNull(builder, "The DockerContextBuilder must not be null");

        // attempt to build the Docker Context
        final Path contextPath = builder.build();

        // TODO: use gzip to compress the Docker Context (if required)

        return build(contextPath, options);
    }
}
