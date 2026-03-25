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

import build.base.configuration.Default;
import build.base.configuration.Option;
import build.base.table.Table;
import build.base.table.Tabular;
import build.spawn.application.Application;

/**
 * An {@link Option} specifying if a launched {@link Application} (child) may be orphaned (left running) when the
 * launching {@link Application} (parent) is terminated.
 * <p>
 * By default {@link Application}s are not orphanable.  When their parent is terminated, they too are terminated.
 *
 * @author brian.oliver
 * @since Nov-2018
 */
public enum Orphanable
    implements Option, Tabular {

    /**
     * Indicates that an {@link Application} is orphanable, meaning that when the launching parent {@link Application}
     * terminates, the {@link Application} is left running, as an orphan.
     */
    ENABLED,

    /**
     * Indicates that an {@link Application} is not orphanable, meaning that when the launching parent
     * {@link Application} terminates, the {@link Application} is also terminated.
     */
    @Default
    DISABLED;

    @Override
    public void tabularize(final Table table) {
        table.addRow("Orphanable?", this == ENABLED ? "Yes" : "No");
    }
}
