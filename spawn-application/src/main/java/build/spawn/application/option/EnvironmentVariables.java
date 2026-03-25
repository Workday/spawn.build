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

import build.base.configuration.ConfigurationBuilder;
import build.base.configuration.Default;
import build.base.configuration.Option;
import build.base.table.Table;
import build.base.table.Tabular;
import build.base.table.option.CellSeparator;
import build.base.table.option.RowComparator;
import build.base.table.option.TableName;
import build.spawn.application.Application;
import build.spawn.application.Customizer;
import build.spawn.application.Platform;
import build.spawn.option.EnvironmentVariable;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * An immutable {@link Option} that defines the initial source of {@link EnvironmentVariable}s to use when launching an
 * {@link Application}.  Further {@link EnvironmentVariable}s may be specified to override those provided by the
 * {@link EnvironmentVariables}.
 *
 * @author brian.oliver
 * @since Oct-2018
 */
public interface EnvironmentVariables
    extends Customizer<Application>, Tabular {

    /**
     * Obtains an {@link EnvironmentVariables} {@link Option} indicating that no initial {@link EnvironmentVariable}s
     * are to be defined for an {@link Application} being launched, beyond those defined by the underlying operating
     * system itself.
     *
     * @return an {@link EnvironmentVariables}
     */
    @Default
    static EnvironmentVariables none() {
        return new None();
    }

    /**
     * Obtains an {@link EnvironmentVariables} {@link Option} indicating that initial {@link EnvironmentVariable}s
     * for an {@link Application} being launched should be obtained from the result of {@link System#getenv()}
     * for the current Java Virtual Machine, in addition to those defined by the underlying operating system itself.
     *
     * @return an {@link EnvironmentVariables}
     */
    static EnvironmentVariables inherited() {
        return new Inherited();
    }

    /**
     * A {@link EnvironmentVariables} representing no initial {@link EnvironmentVariable}s are provided.
     */
    class None
        implements EnvironmentVariables {

        @Override
        public void tabularize(final Table table) {
            // nothing to tabularize
        }
    }

    /**
     * A {@link EnvironmentVariables} that uses those defined by the current Java Virtual Machine the initial source
     * of {@link EnvironmentVariable}s.
     */
    class Inherited
        implements EnvironmentVariables {

        @Override
        public Optional<Supplier<Table>> getTableSupplier() {
            return Optional.of(() -> {
                final Table table = Table.create();
                table.options().add(TableName.of("Inherited Environment Variables"));
                table.options().add(RowComparator.orderByColumn(0));
                table.options().add(CellSeparator.of(" = "));
                return table;
            });
        }

        @Override
        public void tabularize(final Table table) {
            System.getenv().entrySet()
                .forEach(entry -> table.addRow(entry.getKey(), entry.getValue()));
        }

        @Override
        public void onLaunching(final Platform platform,
                                final Class<? extends Application> applicationClass,
                                final ConfigurationBuilder configurationBuilder) {

            // add an EnvironmentVariable for each of the System.getenv()
            System.getenv().entrySet().stream()
                .map(entry -> EnvironmentVariable.of(entry.getKey(), entry.getValue()))
                .forEach(configurationBuilder::add);
        }
    }
}
