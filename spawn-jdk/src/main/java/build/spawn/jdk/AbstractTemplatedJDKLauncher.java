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

import build.base.configuration.ConfigurationBuilder;
import build.base.network.Server;
import build.spawn.application.AbstractTemplatedLauncher;
import build.spawn.application.Platform;
import build.spawn.application.Process;
import build.spawn.application.TemplatedLauncher;
import build.spawn.application.option.Name;
import build.spawn.jdk.agent.SpawnAgent;
import build.spawn.jdk.option.Jar;
import build.spawn.jdk.option.MainClass;
import jakarta.inject.Inject;

/**
 * An abstract {@link TemplatedLauncher} for {@link JDKApplication}s.
 *
 * @param <A> the type of {@link JDKApplication} that will be launched
 * @param <P> the type of {@link Platform} on which the {@link JDKApplication} will be launched
 * @param <N> the type of {@link Process} representing managing the {@link JDKApplication} when it's launched
 * @author brian.oliver
 * @since Nov-2019
 */
public abstract class AbstractTemplatedJDKLauncher<
    A extends JDKApplication,
    P extends Platform,
    N extends Process>
    extends AbstractTemplatedLauncher<A, P, N> {

    /**
     * The {@link Server} to which the launched {@link JDKApplication} {@link SpawnAgent}s will connect.
     */
    @Inject
    protected Server server;

    @Override
    public Name getName(final ConfigurationBuilder options) {

        // attempt to use the MainClass as the name of the application
        final var mainClass = options.get(MainClass.class);

        if (mainClass == null) {
            // attempt to use the Jar filename as the name of the application
            final var jarPath = options.get(Jar.class);

            if (jarPath == null) {
                throw new IllegalArgumentException("Failed to define a MainClass for the JDKApplication");
            }

            return Name.of(jarPath.get().getFileName().toString());
        }
        else {
            final var parts = mainClass.className().split("\\.");
            return Name.of(parts.length < 1 ? mainClass.className() : parts[parts.length - 1]);
        }
    }
}
