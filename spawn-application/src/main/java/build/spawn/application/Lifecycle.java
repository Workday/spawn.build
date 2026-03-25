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

import build.base.foundation.CompletableFutures;

import java.util.concurrent.CompletableFuture;

/**
 * Provides {@link CompletableFuture}s allowing interaction with {@link Object}s that have asynchronous lifecycles.
 *
 * @param <T> the type of returned by the {@link Lifecycle} callback methods, typically the type implementing the
 *            {@link Lifecycle} interface
 * @author graeme.campbell
 * @since Oct-2019
 */
public interface Lifecycle<T> {

    /**
     * Obtains a {@link CompletableFuture} indicating the {@link Object} has started and is operational, providing the
     * ability to either wait for or trigger dependant actions synchronously or asynchronously upon the {@link Object}
     * starting.
     * <p>
     * When an {@link Object} is started, it may not yet be in a state that is considered operational. Calling
     * {@link CompletableFuture#get()} on the returned {@link CompletableFuture} will block the calling thread,
     * forcing it to wait for the {@link Object} to become operational.  Alternatively the {@link CompletableFuture} can
     * be used to check if the {@link Object} is operational without blocking. Attempting to
     * {@link CompletableFuture#cancel(boolean)} the {@link CompletableFuture} has no effect on the {@link Object}.
     * <p>
     * By default, {@link Class}es implementing {@link Lifecycle} are considered started and operational immediately after
     * they are launched.
     *
     * @return a {@link CompletableFuture} for the {@link Object}
     */
    default CompletableFuture<? extends T> onStart() {
        return CompletableFutures.completedFuture();
    }

    /**
     * Obtains a {@link CompletableFuture} indicating the termination of the {@link Object}, providing the ability to
     * either wait for or trigger dependant actions synchronously or asynchronously upon {@link Object} termination.
     * <p>
     * When terminated, the {@link CompletableFuture} will be completed with the {@link Object} which was terminated as
     * a result, thus allowing dependent actions to operate on the terminated {@link Object}.
     * <p>
     * Calling {@link CompletableFuture#get()} on the returned {@link CompletableFuture} will block the calling thread,
     * forcing it to wait for the {@link Object} to terminate. Alternatively the {@link CompletableFuture} can be used
     * to check if the {@link Object} has terminated without blocking. Attempting to
     * {@link CompletableFuture#cancel(boolean)} the {@link CompletableFuture} has no effect on the {@link Object}.
     *
     * @return a {@link CompletableFuture} for the {@link Object}
     */
    CompletableFuture<? extends T> onExit();
}
