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
import build.base.foundation.Introspection;
import build.base.foundation.Preconditions;
import jakarta.inject.Inject;

import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;

/**
 * An application launched by a {@link Platform}.
 *
 * @author Brian Oliver
 * @since Jan-2018
 */
public interface Application
    extends AutoCloseable, Lifecycle<Application> {

    /**
     * Obtains the {@link Class} of {@code interface} of {@link Application}.
     *
     * @return the {@link Class} of {@code interface} of {@link Application}
     */
    Class<? extends Application> getInterfaceClass();

    /**
     * Obtains the name of the {@link Application}.
     *
     * @return the name
     */
    String name();

    /**
     * Obtains the {@link Configuration}s used to launch the {@link Application}.
     *
     * @return the {@link Configuration} for the {@link Application}
     */
    Configuration configuration();

    /**
     * Obtains the {@link Platform} on which the {@link Application} was launched.
     *
     * @return the {@link Platform}
     */
    Platform platform();

    /**
     * Obtains the {@link Console} for the {@link Application}.
     *
     * @return the {@link Optional} {@link Console}
     */
    Optional<Console> console();

    /**
     * Determines if the {@link Application} is alive.
     * <p>
     * The value returned is only representative of a {@link Application} state at the moment the method was invoked.
     * Upon returning, the {@link Application} liveliness state may change from being {@code true} to being
     * {@code false}, however it will never change once {@code false}.
     * <p>
     * This method should not be used to test for or trigger operations based on {@link Application} termination.
     * Instead {@link #onExit()} should be used for these purposes.
     *
     * @return {@code true} if the {@link Application} is alive, {@code false} otherwise
     */
    boolean isAlive();

    /**
     * Requests the {@link Application} to be suspended, that is, placed in a state by the operating system in which it
     * does not run.
     * <p>
     * If the {@link Application} is not alive, the returned {@link CompletableFuture} is completed exceptionally.
     *
     * @return {@link CompletableFuture} indicating when the {@link Application} was suspended
     */
    CompletableFuture<? extends Application> suspend();

    /**
     * Requests that a previously {@link #suspend() suspended} {@link Application} be resumed, that is, made available
     * by the operating system to run again.
     * <p>
     * If the {@link Application} is not alive, the returned {@link CompletableFuture} is completed exceptionally.
     *
     * @return {@link CompletableFuture} indicating when the {@link Application} was resumed
     */
    CompletableFuture<? extends Application> resume();

    /**
     * Requests the {@link Application} to be cleanly shutdown.
     * <p>
     * If the {@link Application} is not alive, no action is taken.
     * <p>
     * Some {@link Application}s may not support clean shutdown, in which case invoking {@link #shutdown()} is identical
     * to invoking {@link #destroy()}.
     * <p>
     * Once this method returns the {@link Application} can be assumed to have been terminated, after which invoking
     * {@link #isAlive()} will return {@code false} and {@link CompletableFuture}s returned by {@link #onExit()} will
     * be completed.
     */
    void shutdown();

    /**
     * Requests the {@link Application} to be terminated forcibly, avoiding clean shutdown.
     * <p>
     * If the {@link Application} is not alive, no action is taken.
     * <p>
     * Once this method returns the {@link Application} can be assumed to have been terminated, after which invoking
     * {@link #isAlive()} will return {@code false} and {@link CompletableFuture}s returned by {@link #onExit()} will
     * be completed.
     */
    void destroy();

    /**
     * Obtains the optionally available exit value for a terminated {@link Application}.
     * <p>
     * Upon termination an {@link Application} may provide an exit value. Typically a value of {@code 0} indicates a
     * normal termination.  However in some circumstances it may not be possible to determine a value, in which
     * case the exit value returned will not be present.
     * <p>
     * This method should not be used to test for or trigger operations based on {@link Application} termination.  A
     * returned value of {@link OptionalInt#empty()} does not indicate {@link Application} termination, either
     * successfully or otherwise, just that the exit value is not available.  Instead {@link #onExit()} should be used
     * for these purposes.
     *
     * @return the {@link OptionalInt} exit value for a {@link Application}
     */
    OptionalInt exitValue();

    /**
     * Requests the {@link Application} to be shutdown gracefully.
     * <p>
     * This is semantically equivalent to invoking {@link #shutdown()}.
     */
    @Override
    default void close() {
        shutdown();
    }

    /**
     * Attempts to obtain the non-abstract {@link Application} {@link Class} that is suitable to be used as the
     * <i>basis</i> for an implementation of the specified {@code interface} {@link Class} of {@link Application}.
     * <p>
     * A suitable implementation {@link Class} is defined as the first public, static, non-abstract class that
     * is defined on an {@code interface} {@link Class} of {@link Application} or one of it's parent {@code interfaces},
     * to which the {@link Class} of {@link Application} is assignable, that is named {@code Implementation}.
     *
     * @param <T>              the type of {@link Application}
     * @param applicationClass the {@code interface} {@link Class} of {@link Application}
     * @return an implementation {@link Class} for the {@link Application}
     */
    @SuppressWarnings("unchecked")
    static <T extends Application> Class<? extends Application> getImplementationClass(final Class<T> applicationClass) {

        Objects.requireNonNull(applicationClass, "The class of Application must not be null");

        Preconditions.require(
            applicationClass,
            applicationClass.isInterface(),
            "The class of Application must be an interface");

        // discover the Application implementation by reflectively finding
        // the first public, static and non-abstract class that is assignable to the class of application
        return Introspection.getAll(applicationClass, Class::getDeclaredClasses)
            .filter(Application.class::isAssignableFrom)
            .filter(c -> c.getSimpleName().equals("Implementation"))
            .filter(c -> {
                final int modifiers = c.getModifiers();
                return !Modifier.isInterface(modifiers) &&
                    !Modifier.isAbstract(modifiers) &&
                    Modifier.isPublic(modifiers) &&
                    Modifier.isStatic(modifiers) &&
                    !c.isEnum() &&
                    !c.isAnnotation();
            })
            .map(c -> (Class<? extends Application>) c)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Failed to locate suitable Implementation class for " +
                applicationClass));
    }

    /**
     * An implementation of an {@link Application}.
     */
    class Implementation
        extends AbstractApplication {

        /**
         * Constructs an {@link Implementation}.
         *
         * @param platform         the {@link Platform}
         * @param process          the {@link Process}
         * @param applicationClass the {@link Class} of {@link Application}
         * @param configuration    the {@link Configuration}
         */
        @Inject
        public Implementation(final Platform platform,
                              final Process process,
                              final Class<? extends Application> applicationClass,
                              final Configuration configuration) {

            super(platform, process, applicationClass, configuration);
        }
    }
}
