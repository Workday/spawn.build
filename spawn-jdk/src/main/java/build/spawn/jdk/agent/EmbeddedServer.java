package build.spawn.jdk.agent;

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
import build.base.network.Client;

import java.io.Serializable;
import java.lang.instrument.Instrumentation;
import java.net.URI;
import java.util.Optional;
import java.util.Properties;

/**
 * The embedded server for the {@link SpawnAgent}, servicing requests from {@code JDKApplication}s.
 *
 * @author brian.oliver
 * @since Nov-2018
 */
public class EmbeddedServer {

    /**
     * The {@link Client} created by the {@link EmbeddedServer}.
     */
    private static volatile Client client;

    /**
     * Starts the {@link EmbeddedServer}.
     *
     * @param properties      the {@link Properties} for the {@link EmbeddedServer}
     * @param instrumentation the {@link Instrumentation} created for loading the {@link EmbeddedServer}
     */
    public void start(final Properties properties,
                      final Instrumentation instrumentation) {

        // determine the launch id (we use this as the client identifier)
        final long launchId = Long.parseLong(properties.getProperty("launchId"));

        // determine if the application is orphanable
        final boolean orphanable = properties.getProperty("orphanable", "DISABLED")
            .trim().equalsIgnoreCase("enabled");

        try {
            System.err.println("[SpawnAgent:" + launchId + "] Discovered Properties " + properties);

            // determine uri of the Machine that started and to which this SpawnAgent should connect
            final URI uri = new URI(properties.getProperty("machine"));

            System.err.println("[SpawnAgent:" + launchId + "] Connecting to " + uri);

            // establish a connection to the launching process
            client = new Client((int) launchId, uri);

            client.onStarted()
                .whenComplete((c, t) -> System.err.println("[SpawnAgent:" + launchId + "] Connected to " + uri));

            // handle when the connection is lost
            client.onStopped()
                .whenComplete((c, t) -> {
                    System.err.println("[SpawnAgent:" + launchId + "] Client Connection Lost.");

                    // terminate the application (when not orphanable)
                    if (!orphanable) {
                        Runtime.getRuntime().halt(0);
                    }
                });
        }
        catch (final Exception e) {
            System.err.println("[SpawnAgent:" + launchId + "] Connection Failure");
            e.printStackTrace();

            // terminate the application when we're not allowed to be an orphan
            if (!orphanable) {
                Runtime.getRuntime().halt(0);
            }
        }

        System.err.println("[SpawnAgent:" + launchId + "] Running...");
    }

    /**
     * Creates a {@link Producer}.
     *
     * @param name      the name of the {@link Producer}
     * @param itemClass the {@link Class} of items that will be published
     * @param <T>       the type of the itemClass
     * @return an {@link Optional} containing the new {@link Producer} or {@link Optional#empty()} when
     * {@link Producer} could not be created.
     */
    public static <T extends Serializable> Optional<Producer<T>> createProducer(final String name,
                                                                                final Class<? extends T> itemClass) {
        return client == null ? Optional.empty() : client.createProducer(name, itemClass);
    }
}
