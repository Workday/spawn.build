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

import build.base.option.TemporaryDirectory;
import build.base.option.WorkingDirectory;

/**
 * An {@link Addressable} {@link Platform}, typically representing an operating system in which {@link Application}s
 * may be launched.
 *
 * @author Brian Oliver
 * @since Jan-2018
 */
public interface Machine
    extends Addressable, Platform {

    /**
     * Obtains the {@link WorkingDirectory} for the {@link Machine}.
     * <p>
     * To override the {@link WorkingDirectory} for a {@link Machine}, the {@link WorkingDirectory} should be added
     * to the {@link #configuration()}.
     *
     * @return the {@link WorkingDirectory}
     */
    default WorkingDirectory getWorkingDirectory() {
        return configuration().getOptional(WorkingDirectory.class)
            .orElseThrow(() -> new RuntimeException("The WorkingDirectory could not be detected or is undefined"));
    }

    /**
     * Obtains the {@link TemporaryDirectory} for the {@link Machine}.
     * <p>
     * To override the {@link TemporaryDirectory} for a {@link Machine}, the {@link TemporaryDirectory} should be added
     * to the {@link #configuration()}.
     *
     * @return the {@link TemporaryDirectory}
     */
    default TemporaryDirectory getTemporaryDirectory() {
        return configuration().getOptional(TemporaryDirectory.class)
            .orElseThrow(() -> new RuntimeException("The TemporaryDirectory could not be detected or is undefined"));
    }
}
