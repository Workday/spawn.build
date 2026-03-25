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

import java.util.Objects;
import java.util.Optional;

/**
 * An abstract {@link java.lang.Process} that provides a {@link Terminal}.
 *
 * @author brian.oliver
 * @since Oct-2018
 */
public abstract class AbstractProcess
    implements Process {

    /**
     * The {@link Terminal}.
     */
    private final Terminal terminal;

    /**
     * Constructs an {@link AbstractProcess}.
     *
     * @param terminal the {@link Terminal} for the {@link java.lang.Process}
     */
    public AbstractProcess(final Terminal terminal) {
        this.terminal = Objects.requireNonNull(terminal, "The Terminal must not be null");
    }

    @Override
    public Optional<Terminal> terminal() {
        return Optional.of(this.terminal);
    }

    /**
     * Performs operations upon termination of the {@link Process}.
     */
    private void onTerminated() {
        // now wait for the Process to exit (by joining on this thread)
        onExit().join();
    }

    @Override
    public void shutdown() {
        onTerminated();
    }

    @Override
    public void destroy() {
        onTerminated();
    }
}
