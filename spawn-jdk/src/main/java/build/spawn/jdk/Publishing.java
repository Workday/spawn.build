package build.spawn.jdk;

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

import build.base.flow.Producer;
import build.spawn.jdk.agent.EmbeddedServer;

import java.io.Serializable;
import java.util.Optional;

/**
 * A factory for {@link Producer}s, allowing information be published via the {@link build.spawn.jdk.agent.SpawnAgent}
 * back to the launching application.
 *
 * @author brian.oliver
 * @since Jan-2025
 */
public class Publishing {

    private Publishing() {
        // no construction
    }

    /**
     * The fully qualified class name of {@code SpawnAgent}.
     */
    private static final String SPAWN_AGENT_CLASSNAME = "build.spawn.jdk.agent.SpawnAgent";

    /**
     * Attempts to create a {@link Producer}.
     *
     * @param name      the name of the {@link Producer}
     * @param itemClass the {@link Class} of items that will be published using the {@link Producer}
     * @param <T>       the type of the itemClass
     * @return an {@link Optional} containing the new {@link Producer} or {@link Optional#empty()} when
     * {@link Producer} could not be created.
     */
    public static <T extends Serializable> Optional<Producer<T>> createProducer(final String name,
                                                                                final Class<? extends T> itemClass) {
        try {
            // check if ColliderAgent is in classpath
            Class.forName(SPAWN_AGENT_CLASSNAME);
            return EmbeddedServer.createProducer(name, itemClass);
        }
        catch (final ClassNotFoundException e) {
            return Optional.empty();
        }
    }
}
