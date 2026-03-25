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
import build.codemodel.injection.InjectionFramework;

import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Stream;

/**
 * Support for creating and using {@link Session}s.
 *
 * @author brian.oliver
 * @since Aug-2021
 */
public class Sessions {

    /**
     * Private Constructor for {@link Sessions}.
     */
    private Sessions() {
    }

    /**
     * Obtains the discovered {@link Session.Factory}s.
     *
     * @param injectionFramework the {@link InjectionFramework} to use for Dependency Injection
     * @return a {@link Stream} of the discovered {@link Session.Factory}s
     */
    public static Stream<? extends Session.Factory> factories(final InjectionFramework injectionFramework) {

        Objects.requireNonNull(injectionFramework, "The InjectionFramework must not be null");

        final var serviceLoaderClassLoader = Sessions.class.getClassLoader();
        final var serviceLoader = ServiceLoader.load(Session.Factory.class, serviceLoaderClassLoader);

        final var context = injectionFramework
            .newContext();

        context.bind(InjectionFramework.class).to(injectionFramework);

        return serviceLoader.stream()
            .map(provider -> {
                try {
                    final var sessionFactory = provider.get();
                    return context.inject(sessionFactory);
                }
                catch (final Exception e) {
                    return null;
                }
            })
            .filter(Objects::nonNull);
    }

    /**
     * Attempt to create a {@link Session} using the first discovered {@link Session.Factory} that can produce a
     * {@link Session} using the provided {@link Option}s.
     *
     * @param injectionFramework the {@link InjectionFramework} to use for Dependency Injection
     * @param configuration      the {@link Session} {@link Configuration}s
     * @return the {@link Optional} {@link Session} or {@link Optional#empty()} should it not be possible to
     * create a {@link Session}
     */
    public static Optional<Session> createSession(final InjectionFramework injectionFramework,
                                                  final Configuration configuration) {

        Objects.requireNonNull(injectionFramework, "The InjectionFramework must not be null");

        return factories(injectionFramework)
            .filter(Session.Factory::isOperational)
            .map(factory -> factory.create(configuration))
            .filter(Optional::isPresent)
            .findFirst()
            .map(Optional::orElseThrow);
    }

    /**
     * Attempt to create a {@link Session} using the first discovered {@link Session.Factory} that can produce a
     * {@link Session} using the provided {@link Option}s.
     *
     * @param injectionFramework the {@link InjectionFramework} to use for Dependency Injection
     * @param options            the {@link Session} {@link Option}s
     * @return the {@link Optional} {@link Session} or {@link Optional#empty()} should it not be possible to
     * create a {@link Session}
     */
    public static Optional<Session> createSession(final InjectionFramework injectionFramework,
                                                  final Option... options) {

        return createSession(injectionFramework, Configuration.of(options));
    }
}
