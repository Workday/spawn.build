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

import build.base.configuration.ConfigurationBuilder;
import build.base.configuration.Option;
import build.base.foundation.CompletableFutures;

import java.util.concurrent.CompletableFuture;

/**
 * A type of {@link Option} allowing the interception of {@link Application} lifecycle transitions, prior to and after
 * launching by a {@link Platform}, and prior to and after termination, allowing bespoke
 * customization of configuration {@link Option}s and behavior.
 *
 * @param <A> the type of {@link Application}
 * @author brian.oliver
 * @since Jan-2018
 */
public interface Customizer<A extends Application>
    extends Option {

    /**
     * Invoked to prepare the provided {@link ConfigurationBuilder} for the specified {@link Class} of
     * {@link Application}.
     *
     * @param platform             the {@link Platform}
     * @param applicationClass     the {@link Class} of {@link Application}
     * @param configurationBuilder the {@link ConfigurationBuilder}
     */
    default void onPreparing(final Platform platform,
                             final Class<? extends A> applicationClass,
                             final ConfigurationBuilder configurationBuilder) {

        // by default there's no special preparation
    }

    /**
     * Invoked prior to launching an {@link Application}, providing an opportunity to create, replace or remove
     * options from the {@link ConfigurationBuilder}.
     *
     * @param platform             the {@link Machine}
     * @param applicationClass     the {@link Class} of {@link Application}
     * @param configurationBuilder the {@link ConfigurationBuilder}
     */
    default void onLaunching(final Platform platform,
                             final Class<? extends A> applicationClass,
                             final ConfigurationBuilder configurationBuilder) {

        // by default there's no special launching operations
    }

    /**
     * Invoked after an {@link Application} was launched, prior to it being returned by a {@link Platform},
     * allowing post launch {@link Application} initialization.
     *
     * @param platform         the {@link Platform}
     * @param applicationClass the {@link Class} of {@link Application}
     * @param application      the {@link Application}
     */
    default void onLaunched(final Platform platform,
                            final Class<? extends A> applicationClass,
                            final A application) {
    }

    /**
     * Obtains a {@link CompletableFuture} indicating the specified {@link Application} has started and is
     * operational with respect to the {@link Customizer}.
     * <p>
     * By default, {@link Application}s are considered started and operational immediately after they are launched.
     *
     * @param platform         the {@link Platform}
     * @param applicationClass the {@link Class} of {@link Application}
     * @param application      the {@link Application}
     * @return a {@link CompletableFuture} for the {@link Object}
     */
    default CompletableFuture<? extends A> onStart(final Platform platform,
                                                   final Class<? extends A> applicationClass,
                                                   final A application) {

        return CompletableFutures.completedFuture();
    }

    /**
     * Invoked prior to an {@link Application} being suspended as a result of invoking {@link Application#suspend()}.
     *
     * @param platform         the {@link Platform}
     * @param applicationClass the {@link Class} of {@link Application}
     * @param application      the {@link Application}
     */
    default void onSuspending(final Platform platform,
                              final Class<? extends A> applicationClass,
                              final A application) {

        // by default there's no special suspend operations
    }

    /**
     * Invoked prior to an {@link Application} being resumed as a result of invoking {@link Application#resume()}.
     *
     * @param platform         the {@link Platform}
     * @param applicationClass the {@link Class} of {@link Application}
     * @param application      the {@link Application}
     */
    default void onResuming(final Platform platform,
                            final Class<? extends A> applicationClass,
                            final A application) {

        // by default there's no special resume operations
    }

    /**
     * Invoked prior to an {@link Application} being shutdown as a result of invoking {@link Application#shutdown()}.
     *
     * @param platform         the {@link Platform}
     * @param applicationClass the {@link Class} of {@link Application}
     * @param application      the {@link Application}
     */
    default void onShuttingDown(final Platform platform,
                                final Class<? extends A> applicationClass,
                                final A application) {

        // by default there's no special shutdown operations
    }

    /**
     * Invoked prior to an {@link Application} being destroyed as a result of invoking {@link Application#destroy()}.
     *
     * @param platform         the {@link Platform}
     * @param applicationClass the {@link Class} of {@link Application}
     * @param application      the {@link Application}
     */
    default void onDestroying(final Platform platform,
                              final Class<? extends A> applicationClass,
                              final A application) {

        // by default there's no special destroy operations
    }

    /**
     * Invoked after an {@link Application} was terminated due to self termination (graceful or abruptly) or
     * as a result of invoking {@link Application#shutdown()} or {@link Application#destroy()}.
     *
     * @param platform         the {@link Platform}
     * @param applicationClass the {@link Class} of {@link Application}
     * @param application      the {@link Application}
     */
    default void onTerminated(final Platform platform,
                              final Class<? extends A> applicationClass,
                              final A application) {

        // by default there's no special termination operations
    }
}
