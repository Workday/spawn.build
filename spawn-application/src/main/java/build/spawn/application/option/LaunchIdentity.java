package build.spawn.application.option;

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

import build.base.configuration.AbstractValueOption;
import build.spawn.application.Application;
import build.spawn.application.Platform;

/**
 * Defines identity of a launched {@link Application} for a specific {@link Platform} instance.
 *
 * @author graeme.campbell
 * @since Jun-2019
 */
public class LaunchIdentity
    extends AbstractValueOption<Long> {

    /**
     * Constructs an {@link LaunchIdentity}.
     *
     * @param id the launch id for an application
     */
    private LaunchIdentity(final long id) {
        super(id);
    }

    /**
     * Creates an {@link LaunchIdentity} for an {@link Application}.
     *
     * @param id the launch identifier for the application
     * @return the constructed {@link LaunchIdentity}
     */
    public static LaunchIdentity of(final long id) {
        return new LaunchIdentity(id);
    }
}
