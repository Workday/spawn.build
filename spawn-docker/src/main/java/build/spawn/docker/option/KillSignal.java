package build.spawn.docker.option;

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

import build.base.configuration.Default;
import build.base.configuration.Option;

/**
 * The POSIX signal sent to a {@link build.spawn.docker.Container} when killing it.
 * <p>
 * Corresponds to the {@code signal} query parameter of the
 * <a href="https://docs.docker.com/engine/api/v1.41/#operation/ContainerKill">Kill Container</a> API.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
public enum KillSignal
    implements Option {

    /**
     * {@code SIGKILL} — immediately terminates the container (Docker's default).
     */
    @Default
    SIGKILL,

    /**
     * {@code SIGTERM} — requests graceful termination, allowing the container to clean up.
     */
    SIGTERM,

    /**
     * {@code SIGQUIT} — requests the container to produce a core dump and terminate.
     */
    SIGQUIT,

    /**
     * {@code SIGHUP} — signals the container to reload its configuration.
     */
    SIGHUP;

    /**
     * Returns the signal name as required by the Docker Engine API.
     *
     * @return the signal name, e.g. {@code "SIGKILL"}
     */
    public String signalName() {
        return name();
    }
}
