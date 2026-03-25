package build.spawn.application;

/*-
 * #%L
 * Spawn Application
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
import build.base.configuration.Option;

import java.util.Objects;

/**
 * A <strong>mutable</strong> mechanism to programmatically specify the {@link Application} {@link Configuration}
 * {@link Option}s, often using <i>fluent-style</i> methods, as the <i>basis</i> to launch <strong>an</strong>
 * <i>instance</i> of a <i>specific</i> {@link Class} of {@link Application} on a {@link Platform}.
 * <p>
 * In addition to any <i>fluent-style</i> methods defined by {@link Specification} implementations,
 * {@link Specification}s provide <i>direct</i> access to their {@link Option}s {@link ConfigurationBuilder}
 * (via the {@link #options()} method), that is used to represent the {@link Specification} {@link Option}s.
 * Importantly, these {@link ConfigurationBuilder}s:
 * <ol>
 *     <li>are safe to be used programmatically to specify additional {@link Configuration} {@link Option}s not directly
 *      supported or configurable by the <i>fluent-style</i> methods, but possibly applicable for launching on a
 *      {@link Platform}, and</li>
 *     <li>are what {@link Platform}s consult as the <i>basis</i> for launching an {@link Application}.</li>
 * </ol>
 * <p>
 * <strong>Note:</strong>
 * <ol>
 *     <li>Any changes to a {@link Specification}, including it's defined {@link Option}s, once it has been used for
 *      launching an {@link Application} are <strong>ignored</strong> by the launching {@link Platform} and launched
 *      {@link Application}.</li>
 *      <li>The {@link Configuration} {@link Option}s ultimately used to launch an {@link Application} by a
 *      {@link Platform} may differ significantly from those provided by {@link Specification} as {@link Platform}s and
 *      {@link Customizer}s are free to override, remove and replace the specified {@link Option}s, in addition to
 *      introducing new {@link Option}s as necessary, typically based on the launch environment, when launching an
 *      {@link Application}.  Such changes <strong>are not reflected</strong> in the {@link Specification} provided
 *      when launching an {@link Application}, but they are available only once an {@link Application} has been launched
 *      via the {@link Application#configuration()} method.</li>
 * </ol>
 * <p>
 * The {@link Configuration} {@link Option}
 *
 * @param <A> the type of {@link Application}
 * @author brian.oliver
 * @see AbstractSpecification
 * @since Nov-2024
 */
public interface Specification<A extends Application> {

    /**
     * Obtains the {@link Class} of {@link Application} being configured.
     *
     * @return the {@link Class} of {@link Application}
     */
    Class<? extends A> getApplicationClass();

    /**
     * Obtains the {@link ConfigurationBuilder} used by the {@link Specification} to programmatically define launch
     * {@link Option}s for a {@link Platform}.
     * <p>
     * These {@link Option}s are used by {@link Platform}s as the basis of the configuration to launch
     * an {@link Application}.  While used by {@link Platform}s, the returned {@link ConfigurationBuilder} will never
     * be modified by {@link Platform}s, thus allowing {@link Specification}s to be reused across launches of
     * {@link Application}s of the same {@link Class}.
     *
     * @return the {@link ConfigurationBuilder}
     */
    ConfigurationBuilder options();

    /**
     * Determines if the {@link Specification}, with it's currently configured {@link Option}s, supports providing
     * the necessary information, and thus launching, an {@link Application} on the specified {@link Platform}.
     *
     * @param platform the {@link Platform}
     * @return {@code true} if the {@link Platform} is supported, {@code false} otherwise
     */
    default boolean isSupported(final Platform platform) {
        return true;
    }

    /**
     * Invoked prior to launching an {@link Application} on a {@link Platform}, providing an opportunity to create,
     * replace or remove the <i>actual</i> launch {@link Option}s from the specified {@link ConfigurationBuilder}.
     *
     * @param platform      the {@link Platform}
     * @param launchOptions the {@link ConfigurationBuilder}
     */
    default void onLaunching(final Platform platform,
                             final ConfigurationBuilder launchOptions) {

        // by default, there's nothing more to configure
    }

    /**
     * Creates a {@link Specification} based on the specified {@link Class} of {@link Application} and
     * configuration {@link Option}s.
     *
     * @param applicationClass the {@link Class} of {@link Application}
     * @param options          the {@link Option}s
     * @param <A>              the type of {@link Application}
     * @return a new {@link Specification} for the {@link Class} of {@link Application}
     */
    static <A extends Application> Specification<A> of(final Class<? extends A> applicationClass,
                                                       final Option... options) {

        Objects.requireNonNull(applicationClass, "The Application class must not be null");

        final var configurationBuilder = ConfigurationBuilder.create(options);

        return new Specification<>() {
            @Override
            public Class<? extends A> getApplicationClass() {
                return applicationClass;
            }

            @Override
            public ConfigurationBuilder options() {
                return configurationBuilder;
            }
        };
    }

    /**
     * Creates a {@link Specification} based on the specified {@link Class} of {@link Application} and
     * {@link ConfigurationBuilder}.
     *
     * @param applicationClass the {@link Class} of {@link Application}
     * @param options          the {@link ConfigurationBuilder}
     * @param <A>              the type of {@link Application}
     * @return a new {@link Specification} for the {@link Class} of {@link Application}
     */
    static <A extends Application> Specification<A> of(final Class<? extends A> applicationClass,
                                                       final ConfigurationBuilder options) {

        Objects.requireNonNull(applicationClass, "The Application class must not be null");

        final var builder = options == null
            ? ConfigurationBuilder.create()
            : options;

        return new Specification<>() {
            @Override
            public Class<? extends A> getApplicationClass() {
                return applicationClass;
            }

            @Override
            public ConfigurationBuilder options() {
                return builder;
            }
        };
    }
}
