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

import build.base.configuration.AbstractValueOption;
import build.base.foundation.Strings;

import java.util.Objects;

/**
 * The required {@code Docker Daemon} API version.
 *
 * @author brian.oliver
 * @since Jun-2021
 */
public class DockerAPIVersion
    extends AbstractValueOption<String> {

    /**
     * Constructs a {@link DockerAPIVersion}.
     *
     * @param version the {@link DockerAPIVersion}
     */
    private DockerAPIVersion(final String version) {
        super(version);
    }

    /**
     * Constructs a {@link DockerAPIVersion}.
     *
     * @param version the version
     * @return a new {@link DockerAPIVersion}
     */
    public static DockerAPIVersion of(final String version) {
        Objects.requireNonNull(version, "The DockerAPIVersion must not be null");

        final String canonicalVersion = Strings.trim(version);

        return new DockerAPIVersion(canonicalVersion.startsWith("v")
            ? canonicalVersion
            : "v" + canonicalVersion);
    }
}
