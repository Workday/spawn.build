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

import build.base.configuration.Configuration;
import build.base.configuration.ConfigurationBuilder;
import build.base.foundation.Capture;
import build.base.io.SerializableCallable;
import build.base.network.Connection;
import build.base.network.Server;
import build.spawn.application.Application;
import build.spawn.application.Platform;
import build.spawn.application.option.ApplicationSubscriber;
import build.spawn.application.option.LaunchIdentity;
import build.spawn.jdk.option.AddModules;
import build.spawn.jdk.option.ClassPath;
import build.spawn.jdk.option.Headless;
import build.spawn.jdk.option.Jar;
import build.spawn.jdk.option.ModulePath;
import build.spawn.jdk.option.PatchModule;
import build.spawn.jdk.option.SystemProperties;
import jakarta.inject.Inject;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * A JDK-based {@link Application}.
 *
 * @author brian.oliver
 * @since Oct-2018
 */
public interface JDKApplication
    extends Application {

    @Override
    CompletableFuture<? extends JDKApplication> onStart();

    @Override
    CompletableFuture<? extends JDKApplication> onExit();

    /**
     * Submits the specified {@link SerializableCallable} for execution by and in the {@link JDKApplication}.
     * <p>
     * The necessary dependencies, immediate and transitive, of the {@link SerializableCallable} must exist on the
     * classpath of the launched {@link JDKApplication}.  No attempt is made to package the dependencies.  Failure
     * to specify and provide the necessary dependencies will result in the returned {@link CompletableFuture}
     * being completed exceptionally, with the causing exception.
     *
     * @param <T>      the type of result
     * @param <C>      the type of the {@link SerializableCallable}
     * @param callable the {@link SerializableCallable}
     * @return a {@link CompletableFuture} containing the result of the execution of the {@link SerializableCallable}
     */
    <T extends Serializable, C extends SerializableCallable<T>> CompletableFuture<T> submit(C callable);

    /**
     * The {@link build.spawn.application.Customizer} to configure a {@link JDKApplication} prior to launch.
     */
    class Customizer
        implements build.spawn.application.Customizer<JDKApplication> {

        @Inject
        private Server server;

        @Override
        @SuppressWarnings("unchecked")
        public void onPreparing(final Platform platform,
                                final Class<? extends JDKApplication> applicationClass,
                                final ConfigurationBuilder options) {

            // by default don't include any SystemProperties
            options.computeIfNotPresent(SystemProperties.class, SystemProperties::none);

            // configure the ClassPath and Module System
            // (unless a Jar option is specified)
            if (!options.isPresent(Jar.class)) {
                final var usingInheritedClassPath = Capture.of(false);

                options.computeIfNotPresent(ClassPath.class, () -> {
                    usingInheritedClassPath.set(true);
                    return ClassPath.inherited();
                });

                // only configure the Module System configuration when we've inherited the ClassPath
                if (usingInheritedClassPath.get()) {
                    // configure the module system
                    ModulePath.inherited()
                        .ifPresent(modulePath -> options.addIfNotPresent(ModulePath.class, modulePath));

                    AddModules.inherited()
                        .ifPresent(addModules -> options.addIfNotPresent(AddModules.class, addModules));

                    PatchModule.detect()
                        .forEach(options::add);
                }
            }

            // by default always use Headless mode
            options.computeIfNotPresent(Headless.class, () -> Headless.ENABLED);

            // subscribe the observers as soon as the connection is established
            this.server.onConnection(options.get(LaunchIdentity.class).get())
                .whenComplete(((connection, throwable) -> {
                    if (throwable == null) {
                        options.stream(ApplicationSubscriber.class)
                            .forEach(applicationSubscriber ->
                                connection.subscribe(
                                    applicationSubscriber.name(),
                                    applicationSubscriber.itemClass(),
                                    applicationSubscriber.subscriber()));
                    }
                }));
        }
    }

    /**
     * An implementation of a {@link JDKApplication}.
     */
    class Implementation
        extends Application.Implementation
        implements JDKApplication {

        /**
         * The {@link Connection} for communicating with the {@link JDKApplication}.
         */
        private final CompletableFuture<Connection> connection;

        /**
         * Constructs a {@link JDKApplication.Implementation}.
         *
         * @param platform         the {@link Platform}
         * @param process          the {@link Process}
         * @param applicationClass the {@link Class} of {@link JDKApplication}
         * @param configuration    the {@link Configuration}
         * @param server           the {@link Server} to which this {@link JDKApplication} will connect
         */
        @Inject
        public Implementation(final Platform platform,
                              final build.spawn.application.Process process,
                              final Class<? extends JDKApplication> applicationClass,
                              final Configuration configuration,
                              final Server server) {

            super(platform, process, applicationClass, configuration);

            // obtain the future connection to the application
            final Optional<LaunchIdentity> launchIdentity = configuration.getOptional(LaunchIdentity.class);
            this.connection = server.onConnection(launchIdentity
                .map(LaunchIdentity::get)
                .orElseGet(process::pid));

            // upon application exit, always cancel the connection 
            // (the connection may never be made if the application terminates abruptly!)
            super.onExit()
                .thenRun(() -> this.connection.cancel(true));
        }

        @Override
        public CompletableFuture<? extends JDKApplication> onStart() {
            return super.onStart()
                .thenCompose(application -> this.connection.thenApply(a -> this));
        }

        @Override
        public CompletableFuture<? extends JDKApplication> onExit() {
            return super.onExit()
                .thenApply(a -> this);
        }

        @Override
        public <T extends Serializable, C extends SerializableCallable<T>> CompletableFuture<T> submit(final C callable) {
            return onStart()
                .thenCompose(application -> this.connection)
                .thenCompose(c -> c.submit(callable));
        }
    }
}
