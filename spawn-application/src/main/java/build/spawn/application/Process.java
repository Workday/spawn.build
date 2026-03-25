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

import build.base.io.Terminal;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;

/**
 * A mechanism to represent, control, manage and interact with a process representing an {@link Application} launched
 * by a {@link Platform}.
 *
 * @author brian.oliver
 * @see Application
 * @see Platform
 * @see Addressable
 * @see Terminal
 * @since Jan-2021
 */
public interface Process
    extends Addressable {

    /**
     * Obtains the native identifier for the {@link Process}.
     *
     * @return the {@link Process} identifier
     */
    long pid();

    /**
     * Determines if the {@link Process} is currently alive.
     * <p>
     * The returned value is only representative of the {@link Process} state at the moment the method invoked.  Upon
     * returning the {@link Process} liveliness state may change from being {@code true} to being {@code false}, however
     * it will never change once {@code false}.
     * <p>
     * This method should not be used to test for or trigger operations based on {@link Process} termination.
     * Instead {@link #onExit()} should be used for these purposes.
     *
     * @return {@code true} if the {@link Process} is alive, {@code false} otherwise
     */
    boolean isAlive();

    /**
     * Obtains the {@link Optional} {@link Terminal} for the {@link Process}.
     * <p>
     * Not all {@link Process}es can provide a {@link Terminal} or a mechanism to interact with a {@link Terminal},
     * and hence it is {@link Optional}.
     *
     * @return the {@link Optional} {@link Terminal}
     */
    Optional<Terminal> terminal();

    /**
     * Obtains the optionally available exit value for a {@link Process}.
     * <p>
     * Upon termination, a {@link Process} may provide an exit value.  Typically, a value of {@code 0} indicates normal
     * termination.  However, in some circumstances it may not be possible to determine any such value, in which case
     * the exit value returned will not be present.
     * <p>
     * This method should not be used to test for or trigger operations based on {@link Process} termination.  A
     * returned value of {@link Optional#empty()} does not indicate termination, either successfully or otherwise,
     * just that the exit value is not available.  Instead {@link #onExit()} should be used for these purposes.
     *
     * @return the {@link Optional} exit value for a {@link Process}
     */
    OptionalInt exitValue();

    /**
     * Creates a {@link CompletableFuture} indicating the termination of the {@link Process}, providing the ability
     * to either wait for or trigger dependant actions synchronously or asynchronously upon {@link Process}
     * termination.
     * <p>
     * When terminated the {@link CompletableFuture} will be completed with the {@link Process} which was
     * terminated, thus allowing dependent actions to operate on the said {@link Process}.
     * <p>
     * Calling {@link CompletableFuture#get()} on the returned {@link CompletableFuture} will block the calling
     * thread, forcing it to wait for the {@link Process} to terminate.  Alternatively the {@link CompletableFuture}
     * can be used to check if the {@link Process} has terminated without blocking.   Attempting to
     * {@link CompletableFuture#cancel(boolean)} the {@link CompletableFuture} has no effect on the {@link Process}.
     *
     * @return a new {@link CompletableFuture} for the {@link Process}
     */
    CompletableFuture<Process> onExit();

    /**
     * Requests the {@link Process} to be suspended, that is, placed in a state in which it does not run or respond.
     * <p>
     * If the {@link Process} is not alive, the returned {@link CompletableFuture} is completed exceptionally.
     *
     * @return {@link CompletableFuture} indicating when the {@link Process} was suspended
     */
    CompletableFuture<Process> suspend();

    /**
     * Requests that a previously {@link #suspend() suspended} {@link Process} be resumed to run again.
     * <p>
     * If the {@link Process} is not alive, the returned {@link CompletableFuture} is completed exceptionally.
     *
     * @return {@link CompletableFuture} indicating when the {@link Process} was resumed
     */
    CompletableFuture<Process> resume();

    /**
     * Requests the {@link Process} to be cleanly shutdown.
     * <p>
     * If the {@link Process} is not alive, no action is taken.
     * <p>
     * Some {@link Process}s may not support clean shutdown, in which case invoking {@link #shutdown()} is identical to
     * invoking {@link #destroy()}.
     */
    void shutdown();

    /**
     * Requests the {@link Process} to be destroyed forcibly, potentially avoiding clean shutdown.
     * <p>
     * If the {@link Process} is not alive, no action is taken.
     */
    void destroy();
}
