package build.spawn.platform.local.jdk.application;

/*-
 * #%L
 * Spawn Local JDK
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

import build.base.table.Table;
import build.base.table.option.CellSeparator;
import build.base.table.option.RowComparator;

/**
 * An application to dump detected diagnostics information to stdout.
 *
 * @author brian.oliver
 * @since Oct-2018
 */
public class DiagnosticsApplication {

    /**
     * A private constructor to ensure no construction.
     */
    private DiagnosticsApplication() {
        // ensure no construction
    }

    /**
     * Application Entry Point.
     *
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        // create a Table to contain the diagnostics information
        final var diagnostics = Table.create();
        diagnostics.options().add(CellSeparator.of(" : "));

        // include the system properties in the diagnostics
        final var systemProperties = Table.create();
        systemProperties.options().add(RowComparator.orderByColumn(0));
        systemProperties.options().add(CellSeparator.of(" = "));

        System.getProperties()
            .stringPropertyNames()
            .forEach(key -> systemProperties.addRow(key, System.getProperty(key)));

        diagnostics.addRow("System Properties", systemProperties.toString());

        // output the diagnostics
        System.out.println(diagnostics);
    }
}
