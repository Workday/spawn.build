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
 * Provides access to the {@link Network}s API for a {@link Session}.
 *
 * @author anand.sankaran
 * @since Aug-2022
 */
public interface Networks {

    /**
     * Given a name or id, get the {@link Network}.
     *
     * @param nameOrId name or id
     * @return the {@link Network}
     */
    Optional<Network.Information> inspect(String nameOrId);

    /**
     * Create a new network.
     *
     * @param name name of the network
     * @return the {@link Network} created
     */
    Optional<Network.Information> create(String name);

    /**
     * Deletes a network.
     *
     * @param name name of the {@link Network} to delete
     * @return {@code true} if deletion was successful
     */
    boolean delete(String name);
}
