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
import build.base.configuration.Option;
import build.spawn.application.Application;

/**
 * An {@link Option} to define the executable of an {@link Application} to launch.
 *
 * @author brian.oliver
 * @since Oct-2018
 */
public class Executable
    extends AbstractValueOption<String> {

    /**
     * Constructs an {@link Executable}.
     *
     * @param executable the executable to launch
     */
    private Executable(final String executable) {
        super(executable);
    }

    /**
     * Creates an {@link Executable} from the specified executable to launch.
     *
     * @param executable the executable to launch
     * @return an {@link Executable}
     */
    public static Executable of(final String executable) {
        return new Executable(executable);
    }
}
