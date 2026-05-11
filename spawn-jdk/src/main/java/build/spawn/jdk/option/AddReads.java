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

import build.base.configuration.CollectedOption;
import build.base.configuration.ConfigurationBuilder;
import build.base.option.JDKVersion;
import build.base.table.Cell;
import build.base.table.Table;
import build.base.table.Tabular;
import build.base.table.option.CellSeparator;
import build.base.table.option.TableName;
import build.spawn.application.Platform;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A {@link JDKOption} to specify a {@code --add-reads=source-module=target-module}.
 *
 * @author reed.vonredwitz
 * @since May-2026
 */
public class AddReads
    implements JDKOption, CollectedOption<List>, Tabular {

    /**
     * The source {@link ModuleName} that is granted readability.
     */
    private final ModuleName sourceModule;

    /**
     * The target {@link ModuleName} to read from.
     */
    private final ModuleName targetModule;

    /**
     * Constructs an {@link AddReads}.
     *
     * @param sourceModule the source {@link ModuleName}
     * @param targetModule the target {@link ModuleName}
     */
    private AddReads(final ModuleName sourceModule,
                     final ModuleName targetModule) {
        this.sourceModule = Objects.requireNonNull(sourceModule, "The source ModuleName must not be null");
        this.targetModule = Objects.requireNonNull(targetModule, "The target ModuleName must not be null");
    }

    @Override
    public boolean isSupported(final JDKVersion jdkVersion,
                               final ConfigurationBuilder options) {

        return jdkVersion.isModular();
    }

    @Override
    public Stream<String> resolve(final Platform platform,
                                  final ConfigurationBuilder options) {

        final var arguments = new ArrayList<String>(2);

        arguments.add("--add-reads");
        arguments.add(this.sourceModule.get() + "=" + this.targetModule.get());

        return arguments.stream();
    }

    @Override
    public Optional<Supplier<Table>> getTableSupplier() {
        return Optional.of(() -> {
            final Table table = Table.create();
            table.options().add(TableName.of("Add Reads"));
            table.options().add(CellSeparator.of("="));
            return table;
        });
    }

    @Override
    public void tabularize(final Table table) {
        table.addRow(Cell.of(this.sourceModule.get()), Cell.of(this.targetModule.get()));
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof final AddReads that)) {
            return false;
        }
        return Objects.equals(this.sourceModule, that.sourceModule)
            && Objects.equals(this.targetModule, that.targetModule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.sourceModule, this.targetModule);
    }

    /**
     * Creates an {@link AddReads}.
     *
     * @param sourceModule the source {@link ModuleName}
     * @param targetModule the target {@link ModuleName}
     * @return a new {@link AddReads}
     */
    public static AddReads of(final ModuleName sourceModule,
                              final ModuleName targetModule) {
        return new AddReads(sourceModule, targetModule);
    }

    /**
     * Creates an {@link AddReads}.
     *
     * @param sourceModule the source module name
     * @param targetModule the target module name
     * @return a new {@link AddReads}
     */
    public static AddReads of(final String sourceModule,
                              final String targetModule) {
        return new AddReads(ModuleName.of(sourceModule), ModuleName.of(targetModule));
    }

    /**
     * Detects the {@link Stream} of {@link AddReads} directives for this Virtual Machine.
     *
     * @return the {@link Stream} of {@link AddReads}
     */
    public static Stream<AddReads> detect() {
        return ManagementFactory.getRuntimeMXBean()
            .getInputArguments()
            .stream()
            .filter(argument -> argument.startsWith("--add-reads="))
            .map(argument -> argument.substring("--add-reads=".length()))
            .map(value -> value.split("=", 2))
            .map(parts -> AddReads.of(parts[0], parts[1]));
    }
}
