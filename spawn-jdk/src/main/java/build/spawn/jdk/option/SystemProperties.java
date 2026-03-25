package build.spawn.jdk.option;

/*-
 * #%L
 * Spawn JDK
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

import build.base.configuration.ConfigurationBuilder;
import build.base.configuration.Default;
import build.base.table.Table;
import build.base.table.Tabular;
import build.base.table.option.CellSeparator;
import build.base.table.option.RowComparator;
import build.base.table.option.TableName;
import build.spawn.application.Customizer;
import build.spawn.application.Platform;
import build.spawn.jdk.JDKApplication;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * An immutable {@link Customizer} that defines the initial source of {@link SystemProperty}s to
 * use when launching a {@link JDKApplication}.  Further {@link SystemProperty}s may be specified to override those
 * provided by the {@link SystemProperties}.
 *
 * @author brian.oliver
 * @since Oct-2018
 */
public interface SystemProperties
    extends Customizer<JDKApplication>, Tabular {

    /**
     * Obtains a {@link SystemProperties} indicating that no initial {@link SystemProperty}s are to be defined for the
     * {@link JDKApplication} being launched, beyond those defined by the Java Virtual Machine itself.
     *
     * @return a {@link SystemProperties}
     */
    @Default
    static SystemProperties none() {
        return new None();
    }

    /**
     * Obtains a {@link SystemProperties} indicating that initial {@link SystemProperty}s for the {@link JDKApplication}
     * being launched should be obtained from the result of {@link System#getProperties()} for the current Java
     * Virtual Machine.
     *
     * @return a {@link SystemProperties}
     */
    static SystemProperties inherited() {
        return new Inherited();
    }

    /**
     * A {@link SystemProperties} representing no initial {@link SystemProperty}s are provided.
     */
    class None
        implements SystemProperties {

        @Override
        public void tabularize(final Table table) {
            // nothing to tabularize
        }
    }

    /**
     * A {@link SystemProperties} that uses those defined by the current Java Virtual Machine the initial source
     * of {@link SystemProperty}s.
     */
    class Inherited
        implements SystemProperties {

        @Override
        public Optional<Supplier<Table>> getTableSupplier() {
            return Optional.of(() -> {
                final Table table = Table.create();
                table.options().add(TableName.of("Inherited System Properties"));
                table.options().add(RowComparator.orderByColumn(0));
                table.options().add(CellSeparator.of(" = "));
                return table;
            });
        }

        @Override
        public void tabularize(final Table table) {
            System.getProperties()
                .stringPropertyNames()
                .forEach(name -> table.addRow(name, System.getProperties().getProperty(name)));
        }

        @Override
        public void onLaunching(final Platform platform,
                                final Class<? extends JDKApplication> applicationClass,
                                final ConfigurationBuilder options) {

            // add a SystemProperty for each of the System.getProperties()
            System.getProperties()
                .stringPropertyNames()
                .stream()
                .map(name -> SystemProperty.of(name, System.getProperties().getProperty(name)))
                .forEach(options::add);
        }
    }
}
