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

import java.util.Optional;

/**
 * Represents a {@code Docker} network.
 *
 * @author anand.sankaran
 * @since Aug-2022
 */
public interface Network {

    /**
     * Obtains the <i>identity</i> of the {@link Network}.
     *
     * @return the {@link Network} identity
     */
    String id();

    /**
     * Obtains the name of the {@link Network}.
     *
     * @return the name
     */
    String name();

    /**
     * Inspects an {@link Network} returning the currently available {@link Information}.  Should {@link Information}
     * not be available, an {@link Optional#empty()} is returned.
     *
     * @return an {@link Optional}
     */
    Optional<Information> inspect();

    /**
     * Represents information regarding a network.
     */
    interface Information {

        /**
         * Obtains the driver used.
         *
         * @return the driver
         */
        String driver();

        /**
         * Obtains the <i>identity</i> of the {@link Network}.
         *
         * @return the {@link Network} identity
         */
        String id();

        /**
         * Name of the network.
         *
         * @return name of the network
         */
        String name();

        /**
         * Returns if {@code IPV6} is enabled.
         *
         * @return {@code true} if {@code IPV6} is enabled
         */
        boolean enableIPv6();
    }
}
