package build.spawn.application.composition;

/*-
 * #%L
 * Spawn Application Composition
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
import build.spawn.application.Application;
import build.spawn.application.Platform;
import build.spawn.application.Specification;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Represents one or more {@link Application}s to be launched as part of a {@link Composition}.
 *
 * @param <A> the type of {@link Application} to launch
 * @author brian.oliver
 * @see Composition
 * @see Composition.Builder
 * @since Jun-2020
 */
public interface Composable<A extends Application> {

    /**
     * Obtains the {@link Class} of {@link Application} to launched as part of the {@link Composition}.
     *
     * @return the {@link Class} of {@link Application}
     */
    Class<? extends A> getApplicationClass();

    /**
     * Obtains the {@link ConfigurationBuilder}s to be used to launch the {@link Composable}.
     *
     * @return the {@link ConfigurationBuilder} for the {@link Application}
     */
    ConfigurationBuilder options();

    /**
     * Adds the specified {@link Option}(s) to the {@link Composable}.
     *
     * @param options the {@link Option}(s)
     * @return this {@link Composable} to support fluent-style method invocation
     */
    Composable<A> with(Option... options);

    /**
     * Adds a configuration for the {@link Composable}.
     *
     * @param specification the {@link Specification}
     * @return this {@link Composable} to support fluent-style method invocation
     */
    Composable<A> with(Specification<A> specification);

    /**
     * Adds the {@link Option}s in the specified {@link Configuration} to the {@link Composable}.
     *
     * @param configuration the {@link Configuration}
     * @return this {@link Composable} to support fluent-style method invocation
     */
    Composable<A> with(Configuration configuration);

    /**
     * Sets the number of {@link Application}s to launch as part of the {@link Composition}.
     *
     * @param count the number of {@link Application}s to launch
     * @return this {@link Composable} to support fluent-style method invocation
     */
    Composable<A> launch(int count);

    /**
     * Sets the {@link Platform} on which the {@link Application}s are to be launched.
     *
     * @param platform the {@link Platform}
     * @return this {@link Composable} to support fluent-style method invocation
     */
    Composable<A> using(Platform platform);

    /**
     * Specifies that this {@link Composable} requires the {@link Application}s defined by the specified
     * {@link Composable} to satisfy the provided constraint, in order for it to be launched, thus allowing a
     * {@link Composition} to coordinate and order the launching of {@link Application}s.
     * <p>
     * The constraint is a {@link Function} that produces a {@link CompletableFuture} for an {@link Application}.
     * When the returned {@link CompletableFuture} is completed successfully, the said {@link Application} is assumed
     * to be available for use by {@link Application}s produce by this {@link Comparable}.
     * <p>
     * Typically, developers will simply use the {@link #require(Composable)} method, which defaults to using the
     * {@link Application#onStart()} method as the constraint {@link Function}.
     *
     * @param <T>        the type of the required {@link Composable}
     * @param composable the required {@link Composable}
     * @param constraint the {@link Function} producing a {@link CompletableFuture} to be completed
     * @return the {@link Composable} to support fluent-style method invocation
     */
    <T extends Application> Composable<A> require(Composable<T> composable,
                                                  Function<? super T, CompletableFuture<?>> constraint);

    /**
     * Specifies that this {@link Composable} requires the specified {@link Composable} in order for it to be launched
     * once the {@link Application#onStart()} {@link CompletableFuture}s have successfully completed, thus allowing a
     * {@link Composition} to coordinate and order the launching of {@link Application}s.
     *
     * @param composable the required {@link Composable}
     * @return the {@link Composable} to support fluent-style method invocation
     */
    default Composable<A> require(final Composable<?> composable) {
        return require(composable, Application::onStart);
    }
}
