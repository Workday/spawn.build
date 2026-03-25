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
import build.base.configuration.Default;
import build.base.configuration.Option;
import build.base.table.Table;
import build.base.table.Tabular;
import build.spawn.application.Application;

import java.util.UUID;

/**
 * An {@link Option} to define the name of an {@link Application} to launch.
 *
 * @author brian.oliver
 * @since Oct-2018
 */
public class Name
    extends AbstractValueOption<String>
    implements Tabular {

    /**
     * Constructs a {@link Name}.
     *
     * @param name the name
     */
    private Name(final String name) {
        super(name);
    }

    /**
     * Creates a {@link Name}.
     *
     * @param name the name
     * @return a {@link Name}
     */
    public static Name of(final String name) {
        return new Name(name);
    }

    /**
     * Obtains a uniquely generated {@link Name}.
     *
     * @return a uniquely generated {@link Name}
     */
    @Default
    public static Name unique() {
        return new Name(UUID.randomUUID().toString());
    }

    @Override
    public void tabularize(final Table table) {
        table.addRow("Application Name", get());
    }
}
