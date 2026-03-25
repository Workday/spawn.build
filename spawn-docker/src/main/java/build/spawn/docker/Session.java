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
import build.base.flow.Publisher;

import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Provides the ability to interact with a <a href="https://www.docker.com">Docker</a> Engine using common
 * {@link Option}s and authentication information.
 *
 * @author brian.oliver
 * @since Jun-2021
 */
public interface Session
    extends AutoCloseable {

    /**
     * Inspects the <a href="https://docs.docker.com/engine/api/v1.41/#operation/SystemInfo">System Information</a>
     * available to the {@link Session}.
     *
     * @return the System {@link Information}
     */
    Information inspect();

    /**
     * Obtains the {@link Image}s API for a {@link Session}.
     *
     * @return the {@link Images} API
     */
    Images images();

    /**
     * Obtains the {@link Network}s API for a {@link Session}.
     *
     * @return the {@link Network} API
     */
    Networks networks();

    /**
     * Obtains a {@link Publisher} on which to subscribe and receive {@code Docker Engine} {@link Event}s.
     *
     * @return a {@link Publisher} of {@link Event}s
     */
    Publisher<Event> events();

    @Override
    void close();

    /**
     * Represents immutable <a href="https://docs.docker.com/engine/api/v1.41/#operation/SystemInfo">System Information</a>
     */
    interface Information {

        /**
         * Obtains the version of the Docker Engine (Server).
         *
         * @return the version
         */
        String getServerVersion();
    }

    /**
     * A mechanism to create a {@link Session}.
     * <p>
     * {@link Factory}s that designed to be discovered using the {@link ServiceLoader} pattern.  Consequently
     * implementations must provide a public no argument constructor, that can be used to instantiate a
     * {@link Factory}.
     * </p>
     */
    @FunctionalInterface
    interface Factory {

        /**
         * Determines if the {@link Factory} is operational and capable of producing {@link Session}s.
         *
         * @return {@code true} if operational, {@code false} if not operational
         */
        default boolean isOperational() {
            return true;
        }

        /**
         * Attempts to create a {@link Session}.
         *
         * @param configuration the {@link Configuration} for creating the {@link Session}
         * @return the {@link Optional} {@link Session} or
         * {@link Optional#empty()} if {@link Factory} fails to create the {@link Session}
         */
        Optional<Session> create(Configuration configuration);

        /**
         * Attempts to create a {@link Session}.
         *
         * @param options the {@link Option}s for creating the {@link Session}
         * @return the {@link Optional} {@link Session} or
         * {@link Optional#empty()} if {@link Factory} fails to create the {@link Session}
         */
        default Optional<Session> create(final Option... options) {
            return create(Configuration.of(options));
        }
    }
}
