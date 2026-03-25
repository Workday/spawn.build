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

import build.base.configuration.Option;
import build.spawn.application.Application;
import build.spawn.application.Lifecycle;

import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * An {@link Application} specific {@link java.util.stream.Stream} supporting aggregation operations
 * over {@link Application}s.
 *
 * @param <A> the type of {@link Application}
 * @author brian.oliver
 * @since Jan-2018
 */
public interface ApplicationStream<A extends Application>
    extends Stream<A>, Lifecycle<ApplicationStream<A>> {

    /**
     * Requests the {@link Application}s in the {@link ApplicationStream} to be suspended with
     * {@link Application#suspend()}.
     *
     * @return a {@link CompletableFuture} containing a {@link ApplicationStream} of the {@link Application}s
     * on which {@link Application#suspend()} was invoked
     * @see Application#suspend()
     */
    CompletableFuture<ApplicationStream<A>> suspend();

    /**
     * Requests the {@link Application}s in the {@link ApplicationStream} to be resumed with
     * {@link Application#resume()}.
     *
     * @return a {@link CompletableFuture} containing a {@link ApplicationStream} of the {@link Application}s
     * on which {@link Application#resume()} ()} was invoked
     * @see Application#resume()
     */
    CompletableFuture<ApplicationStream<A>> resume();

    /**
     * Requests the {@link Application}s in the {@link ApplicationStream} be cleanly shutdown.
     * <p>
     * If an {@link Application} is not alive, no action is taken.
     * <p>
     * Once this method returns, all {@link Application}s in the returned {@link ApplicationStream} can be
     * assumed to have been terminated.
     *
     * @return an {@link ApplicationStream} of the {@link Application}s that were shutdown
     * @see Application#shutdown()
     */
    ApplicationStream<A> shutdown();

    /**
     * Requests the {@link Application}s in the {@link ApplicationStream} to be terminated forcibly, avoiding
     * clean shutdown.
     * <p>
     * If an {@link Application} is not alive, no action is taken.
     * <p>
     * Once this method returns, all {@link Application}s in the {@link ApplicationStream} can be assumed to have
     * been terminated.
     *
     * @return an {@link ApplicationStream} of the {@link Application}s that were destroyed
     * @see Application#destroy()
     */
    ApplicationStream<A> destroy();

    /**
     * Obtains an {@link ApplicationStream} containing only {@link Application}s of the specified {@link Class}.
     *
     * @param <T>              the type of {@link Application}
     * @param applicationClass the {@link Class} of {@link Application}
     * @return an {@link ApplicationStream} of the specified {@link Class} of {@link Application}
     */
    default <T extends A> ApplicationStream<T> filter(final Class<T> applicationClass) {
        return (ApplicationStream<T>) filter(applicationClass::isInstance).map(applicationClass::cast);
    }

    /**
     * Requests the {@link Application}s in the {@link ApplicationStream} to be shutdown (if not already
     * terminated) and relaunched using their original launch {@link Option}s, returning an
     * {@link ApplicationStream} of the newly launched {@link Application}s.
     *
     * @param options {@link Option}s to override original launch {@link Option}s.
     * @return an {@link ApplicationStream} of the restarted {@link Application}s.
     */
    ApplicationStream<A> relaunch(Option... options);

    /**
     * Requests the {@link Application}s in the {@link ApplicationStream} to be cloned the specified number of
     * times, with their original launch {@link Option}s, returning an
     * {@link ApplicationStream} of the newly launched {@link Application}s.
     *
     * @param count   the number of clones to create
     * @param options {@link Option}s to override original launch {@link Option}s.
     * @return an {@link ApplicationStream} of the cloned {@link Application}s.
     */
    ApplicationStream<A> clone(int count, Option... options);

    /**
     * Requests the {@link Application}s in the {@link ApplicationStream} to be cloned once, with their original
     * launch {@link Option}s, returning an {@link ApplicationStream} of the newly launched {@link Application}s.
     *
     * @param options {@link Option}s to override original launch {@link Option}s.
     * @return an {@link ApplicationStream} of the cloned {@link Application}s.
     */
    default ApplicationStream<A> clone(final Option... options) {
        return clone(1, options);
    }

    @Override
    ApplicationStream<A> filter(Predicate<? super A> predicate);

    @Override
    ApplicationStream<A> distinct();

    @Override
    ApplicationStream<A> sorted();

    @Override
    ApplicationStream<A> sorted(Comparator<? super A> comparator);

    @Override
    ApplicationStream<A> peek(Consumer<? super A> action);

    @Override
    ApplicationStream<A> limit(long maxSize);

    @Override
    ApplicationStream<A> skip(long n);

    @Override
    ApplicationStream<A> sequential();

    @Override
    ApplicationStream<A> parallel();

    @Override
    ApplicationStream<A> unordered();

    @Override
    ApplicationStream<A> onClose(Runnable closeHandler);
}
