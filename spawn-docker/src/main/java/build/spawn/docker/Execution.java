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
import build.base.io.Terminal;
import build.spawn.docker.option.Command;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;

/**
 * Represents an {@code exec} command that is running or has run inside a {@link Container}.  {@link Execution}s
 * are created by {@link Executable}s, that in turn are provided by the {@link Container#createExecutable(Command)}
 * methods.
 *
 * @author brian.oliver
 * @see Executable
 * @see Container
 * @since Sep-2021
 */
public interface Execution {

    /**
     * Obtains the {@link Container} that launched the {@link Execution}.
     *
     * @return the {@link Container}
     */
    Container container();

    /**
     * Obtains the identity of the {@link Execution} inside the {@link Container}.
     *
     * @return the identity of the {@link Execution}
     */
    String id();

    /**
     * Obtains the {@link Configuration} used to establish the {@link Execution}.
     *
     * @return the {@link Configuration}
     */
    Configuration configuration();

    /**
     * Obtains the {@link Optional} {@link Terminal} established for the {@link Execution}.
     *
     * @return the {@link Optional} {@link Terminal}
     */
    Optional<Terminal> terminal();

    /**
     * Inspects an {@link Execution} returning a snapshot of the currently available {@link Information}.
     * Should no {@link Information} be available, an {@link Optional#empty()} is returned.
     *
     * @return an {@link Optional}
     */
    Optional<Information> inspect();

    /**
     * Obtains the optionally available exit value for a {@link Execution}.
     * <p>
     * Upon termination, a {@link Execution} may provide an exit value.  Typically, a value of {@code 0} indicates
     * normal termination.  However, in some circumstances it may not be possible to determine any such value,
     * in which case the exit value returned will not be present.
     * <p>
     * This method should not be used to test for or trigger operations based on {@link Execution} termination.  A
     * returned value of {@link Optional#empty()} does not indicate termination, either successfully or otherwise,
     * just that the exit value is not available.  Instead {@link #onExit()} should be used for these purposes.
     *
     * @return the {@link Optional} exit value for a {@link Execution}
     */
    default OptionalInt exitValue() {
        return inspect()
            .map(Information::exitValue)
            .orElseGet(OptionalInt::empty);
    }

    /**
     * Creates a {@link CompletableFuture} indicating the termination of the {@link Execution}, providing the ability
     * to either wait for or trigger dependant actions synchronously or asynchronously upon {@link Execution}
     * termination.
     * <p>
     * When terminated the {@link CompletableFuture} will be completed with the {@link Execution} which was
     * terminated, thus allowing dependent actions to operate on the said {@link Execution}.
     * <p>
     * Calling {@link CompletableFuture#get()} on the returned {@link CompletableFuture} will block the calling
     * thread, forcing it to wait for the {@link Execution} to terminate.  Alternatively the {@link CompletableFuture}
     * can be used to check if the {@link Execution} has terminated without blocking.   Attempting to
     * {@link CompletableFuture#cancel(boolean)} the {@link CompletableFuture} has no effect on the {@link Execution}.
     *
     * @return a new {@link CompletableFuture} for the {@link Execution}
     */
    CompletableFuture<Execution> onExit();

    /**
     * Represents information concerning an {@link Execution}.
     */
    interface Information {

        /**
         * Obtains the identity of the {@link Container} in which the {@link Execution} is or has occurred.
         *
         * @return the identity of the {@link Container}
         */
        String getContainerId();

        /**
         * Obtains the identity of the {@link Execution}.
         *
         * @return the identity of the {@link Execution}
         */
        String id();

        /**
         * Attempts to obtain the native process id for {@link Execution}.
         * <p>
         * Depending on the state of the native process, the id for an {@link Execution} may not always be available.
         *
         * @return the {@link OptionalLong} of native process id or {@link OptionalLong#empty()} if not available
         */
        OptionalLong pid();

        /**
         * Obtains the optionally available exit value for a {@link Execution}.
         * <p>
         * Upon termination, a {@link Execution} may provide an exit value.  Typically, a value of {@code 0} indicates
         * normal termination.  However, in some circumstances it may not be possible to determine any such value,
         * in which case the exit value returned will not be present.
         * <p>
         * This method should not be used to test for or trigger operations based on {@link Execution} termination.  A
         * returned value of {@link Optional#empty()} does not indicate termination, either successfully or otherwise,
         * just that the exit value is not available.  Instead {@link #onExit()} should be used for these purposes.
         *
         * @return the {@link Optional} exit value for a {@link Execution}
         */
        OptionalInt exitValue();
    }
}
