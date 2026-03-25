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

import build.base.configuration.Option;
import build.base.io.Terminal;

/**
 * Provides a mechanism to represent and commence execution of commands inside a {@link Container}.
 *
 * @author brian.oliver
 * @since Sep-2021
 */
public interface Executable {

    /**
     * Includes the specified {@link Option} for the {@link Execution}.
     *
     * @param option the {@link Option}
     * @return this {@link Executable} to permit fluent-style method invocation
     */
    Executable with(Option option);

    /**
     * Configures the {@link Executable} on whether to establish a {@link Terminal} for the {@link Execution}.
     * <p>
     * By default, the {@link Executable} will not establish a {@link Terminal} for an {@link Execution}.
     *
     * @param enabled {@code true} means the {@link Executable} will establish a {@link Terminal}, {@code false} means
     *                a {@link Terminal} will not be established
     * @return this {@link Executable} to permit fluent-style method invocation
     */
    Executable withTerminal(boolean enabled);

    /**
     * Executes the command defined by the {@link Executable}, creating an {@link Execution} representing
     * the running command.
     * <p>
     * This method may be used multiple times to execute the same command as needed.
     *
     * @return the {@link Execution} created by the {@link Executable}
     */
    Execution execute();
}
