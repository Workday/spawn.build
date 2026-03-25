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
import build.spawn.docker.option.ExposedPort;
import build.spawn.docker.option.ImageName;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Represents a {@code Docker Image}.
 *
 * @author brian.oliver
 * @since Jun-2021
 */
public interface Image {

    /**
     * Obtains the <i>identity</i> of the {@link Image}.
     *
     * @return the {@link Image} identity
     */
    String id();

    /**
     * Creates and starts a {@link Container} for the {@link Image} using the specified {@link Configuration}.
     * <p>
     * <strong>Important:</strong>  To determine when the {@link Container} has started, consult the
     * {@link CompletableFuture} returned by {@link Container#onStart()}.
     *
     * @param configuration the {@link Configuration}
     * @return the created and starting {@link Container}
     */
    Container start(Configuration configuration);

    /**
     * Creates and starts a {@link Container} for the {@link Image} using the specified {@link Option}s.
     * <p>
     * <strong>Important:</strong>  To determine when the {@link Container} has started, consult the
     * {@link CompletableFuture} returned by {@link Container#onStart()}.
     *
     * @param options the {@link Option}s
     * @return the created and starting {@link Container}
     */
    default Container start(final Option... options) {
        return start(Configuration.of(options));
    }

    /**
     * Removes the {@link Image} for which there are no {@link Container}s running.
     *
     * @param configuration the {@link Configuration}
     */
    void remove(Configuration configuration);

    /**
     * Removes the {@link Image} for which there are no {@link Container}s running.
     *
     * @param options the {@link Option}s
     */
    default void remove(final Option... options) {
        remove(Configuration.of(options));
    }

    /**
     * Removes the {@link Image} for which there are no {@link Container}s running.
     */
    default void remove() {
        remove(Configuration.empty());
    }

    /**
     * Obtains the {@link ImageName}s for the {@link Image}.
     *
     * @return a {@link Stream} of {@link ImageName}s
     */
    Stream<ImageName> names();

    /**
     * Obtains the {@code tags} for the {@link Image}.
     *
     * @return a {@link Stream} of {@code tag}s for the {@link Image}
     */
    default Stream<String> tags() {
        return names()
            .map(ImageName::tag)
            .filter(Optional::isPresent)
            .map(Optional::get);
    }

    /**
     * Inspects an {@link Image} returning the currently available {@link Information}.  Should {@link Information}
     * not be available, an {@link Optional#empty()} is returned.
     *
     * @return an {@link Optional}
     */
    Optional<Information> inspect();

    /**
     * Represents information concerning the current state of an {@link Image}.
     */
    interface Information {

        /**
         * Obtains the identity of the {@link Image}.
         *
         * @return the identity of the {@link Image}
         */
        String imageId();

        /**
         * Obtains a {@link Stream} of the {@link ExposedPort}s for an {@link Image}.
         *
         * @return a {@link Stream} of the {@link ExposedPort}s
         */
        Stream<ExposedPort> exposedPorts();
    }
}
