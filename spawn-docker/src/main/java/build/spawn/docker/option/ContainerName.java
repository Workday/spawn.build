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

import java.util.Objects;

/**
 * An optional option that specifies the name of a docker container. The name of the docker container has to be unique.
 * If not, the creation of the container throws a {@code 409} error.
 *
 * @author anand.sankaran
 * @since Aug-2022
 */
public class ContainerName
    extends AbstractValueOption<String> {

    /**
     * Constructs a {@link ContainerName}.
     *
     * @param name the name of the container
     */
    private ContainerName(final String name) {
        super(Objects.requireNonNull(name, "The Container name must not be null").trim());
    }

    /**
     * Constructs an {@link ContainerName} given the name .
     *
     * @param name the name
     * @return a new {@link ContainerName}
     */
    public static ContainerName of(final String name) {
        return new ContainerName(name);
    }

}
