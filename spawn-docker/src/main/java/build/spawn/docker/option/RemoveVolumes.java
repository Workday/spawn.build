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
import build.base.configuration.Default;

/**
 * An {@link build.base.configuration.Option} allowing {@link Command}s to specify whether they should also remove
 * associated Docker Volumes.
 *
 * @author graeme.hendrickson
 * @since Nov-2022
 */
public class RemoveVolumes
    extends AbstractValueOption<Boolean> {

    /**
     * Constructs a {@link RemoveVolumes}.
     *
     * @param removeVolumes whether the associated {@link Command} should remove volumes
     */
    private RemoveVolumes(final boolean removeVolumes) {
        super(removeVolumes);
    }

    /**
     * Creates a new {@link RemoveVolumes}.
     *
     * @param removeVolumes whether the associated {@link Command} should remove volumes
     * @return a new {@link RemoveVolumes}
     */
    public static RemoveVolumes of(final boolean removeVolumes) {
        return new RemoveVolumes(removeVolumes);
    }

    /**
     * Creates a new {@link RemoveVolumes} with the default (true) value.
     *
     * @return a new, default {@link RemoveVolumes}
     */
    @Default
    public static RemoveVolumes ofDefault() {
        return RemoveVolumes.of(true);
    }
}
