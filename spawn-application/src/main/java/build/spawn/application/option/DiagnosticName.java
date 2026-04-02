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
 * An immutable {@link Option} defining a diagnostic name for an {@link Application}, to be used for identifying
 * specific {@link Application}s when using {@link StandardOutputFormatter} and {@link StandardErrorFormatter}s.
 *
 * @author brian.oliver
 * @since Oct-2018
 */
public class DiagnosticName
    extends AbstractValueOption<String> {

    /**
     * Constructs a {@link DiagnosticName}.
     *
     * @param name the diagnostic name
     */
    private DiagnosticName(final String name) {
        super(name);
    }

    /**
     * Creates a {@link DiagnosticName} for an {@link Application}.
     *
     * @param name the diagnostic name
     * @return a {@link DiagnosticName}
     */
    public static DiagnosticName of(final String name) {
        return new DiagnosticName(name);
    }
}
