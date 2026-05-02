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

import build.base.configuration.Option;

/**
 * Sealed marker interface for options that influence a {@code Docker Engine} request body.
 * <p>
 * Serialization of each concrete type is handled entirely by the command that issues the
 * request, keeping Jackson (and any future JSON library) out of the public API.
 * <p>
 * The sealed hierarchy ensures that any new subtype must be handled exhaustively wherever
 * a pattern-matching switch over {@link DockerOption} is used — enforced at compile time.
 *
 * @author brian.oliver
 * @since Jun-2021
 */
public sealed interface DockerOption
    extends Option
    permits Bind, Command, ExposedPort, ExtraHost, Link, PublishAllPorts, PublishPort {
}
