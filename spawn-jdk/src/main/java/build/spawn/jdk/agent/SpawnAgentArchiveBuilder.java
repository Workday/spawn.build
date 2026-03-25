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

import build.base.archiving.JarBuilder;
import build.base.flow.Producer;
import build.base.flow.Publicist;
import build.base.flow.Publisher;
import build.base.flow.Subscriber;
import build.base.flow.Subscription;
import build.base.io.SerializableCallable;
import build.base.network.Client;
import build.base.network.Connection;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.jar.Attributes;

/**
 * A builder to dynamically construct a {@code jar} archive containing the {@link SpawnAgent}.
 *
 * @author brian.oliver
 * @since Jan-2019
 */
public final class SpawnAgentArchiveBuilder {

    /**
     * Prevent instantiation of the {@link SpawnAgentArchiveBuilder}.
     */
    private SpawnAgentArchiveBuilder() {
    }

    /**
     * Obtains the {@link Path} to the {@link SpawnAgent} that was started with the current Java process
     * using {@code -javaagent=}.
     *
     * @return the {@link Optional} {@link Path} of the {@link SpawnAgent} or
     * {@link Optional#empty()} if not started
     */
    public static Optional<Path> getArchive() {
        final String javaAgentPrefix = "-javaagent:";

        final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

        return runtimeMXBean.getInputArguments().stream()
            .filter(argument -> argument.startsWith(javaAgentPrefix) && argument.contains(
                SpawnAgent.ARCHIVE_NAME))
            .map(argument -> argument.substring(javaAgentPrefix.length()))
            .map(argument -> argument.substring(0, argument.indexOf("=")))
            .map(Paths::get)
            .findFirst();
    }

    /**
     * Creates a Java Archive containing the {@link SpawnAgent} and necessary resources.
     *
     * @return the {@link Path} to the created {@link SpawnAgent} Java Archive
     */
    public static Path createArchive() {

        final JarBuilder builder = new JarBuilder();

        builder.withManifestVersion("1.0.0");
        builder.withSpecificationTitle("build.spawn.jdk.agent");
        builder.withSpecificationVendor(System.getProperty("user.name", "(unknown)"));

        builder.withMainAttribute(new Attributes.Name("Premain-Class"), SpawnAgent.class.getName());
        builder.withMainAttribute(new Attributes.Name("Agent-Class"), SpawnAgent.class.getName());
        builder.withMainAttribute(new Attributes.Name("Can-Redefine-Classes"), "false");
        builder.withMainAttribute(new Attributes.Name("Can-Retransform-Classes"), "false");
        builder.withMainAttribute(new Attributes.Name("Can-Set-Native-Method-Prefix"), "false");

        builder.withMainAttribute(new Attributes.Name("Automatic-Module-Name"), "build.spawn.jdk.agent");
        // TODO? builder.withMainAttribute(new Attributes.Name("Add-Exports"), "");

        // only include the classes we need!
        builder.content()
            .add(SpawnAgent.class)
            .add(SpawnAgent.CustomClassLoader.class);

        // we place the "internal" classes, in the "internal" folder,
        // so we can isolate and load them using a custom ClassLoader
        builder.in("internal")
            .add(EmbeddedServer.class)
            .add(Client.class)
            .add(Connection.class)
            .add(Subscriber.class)
            .add(Publicist.class)
            .add(Producer.class)
            .add(Publisher.class)
            .add(Subscription.class)
            .add(SerializableCallable.class);

        // establish the temporary folder to contain the archive
        try {
            final Path directory = Files.createTempDirectory("build.spawn.");
            final Path archive = directory.resolve(SpawnAgent.ARCHIVE_NAME);

            builder.build(archive);

            // return the Path to the archive
            return archive;
        }
        catch (final IOException e) {
            throw new RuntimeException("Failed to create " + SpawnAgent.ARCHIVE_NAME, e);
        }
    }
}
