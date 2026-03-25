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
import build.base.foundation.Strings;
import build.base.option.JDKVersion;
import build.base.table.Table;
import build.base.table.Tabular;
import build.base.table.option.CellSeparator;
import build.base.table.option.TableName;
import build.spawn.application.Platform;
import build.spawn.jdk.JDKApplication;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * An immutable {@link JDKOption} to represent the modules to add when launching a {@link JDKApplication}.
 *
 * @author brian.oliver
 * @since Jan-2025
 */
public class AddModules
    implements JDKOption, Tabular {

    /**
     * The {@link ModuleName}s to be added.
     */
    private final LinkedHashSet<ModuleName> moduleNames;

    /**
     * An empty {@link AddModules}.
     */
    private static final AddModules EMPTY = new AddModules(Stream.empty());

    /**
     * Constructs an {@link AddModules} from a {@link Stream} of {@link ModuleName}s.
     *
     * @param moduleNames the {@link ModuleName}s
     */
    private AddModules(final Stream<ModuleName> moduleNames) {
        this.moduleNames = new LinkedHashSet<>();
        if (moduleNames != null) {
            moduleNames.filter(Objects::nonNull)
                .forEach(this.moduleNames::add);
        }
    }

    /**
     * Determines if there are any {@link ModuleName}s to add.
     *
     * @return {@code true} if there are {@link ModuleName}s to add, {@code false} otherwise
     */
    public boolean isEmpty() {
        return this.moduleNames.isEmpty();
    }

    /**
     * Determines the number of {@link ModuleName}s to add.
     *
     * @return the number of {@link ModuleName}s
     */
    public int size() {
        return this.moduleNames.size();
    }

    /**
     * Obtains the {@link ModuleName}s to be added.
     *
     * @return a {@link Stream} of {@link ModuleName}s
     */
    public Stream<ModuleName> stream() {
        return this.moduleNames.stream();
    }

    @Override
    public boolean hasTabularContent() {
        return !this.moduleNames.isEmpty();
    }

    @Override
    public Optional<Supplier<Table>> getTableSupplier() {
        return Optional.of(() -> {
            final Table table = Table.create();
            table.options().add(TableName.of("Modules Added"));
            table.options().add(CellSeparator.of(" : "));
            return table;
        });
    }

    @Override
    public void tabularize(final Table table) {
        this.moduleNames.forEach(moduleName -> table.addRow(moduleName.get()));
    }

    @Override
    public boolean isSupported(final JDKVersion jdkVersion,
                               final ConfigurationBuilder options) {

        return jdkVersion.isModular();
    }

    @Override
    public Stream<String> resolve(final Platform platform,
                                  final ConfigurationBuilder options) {

        // don't attempt to add the Module Name when there's none
        if (this.moduleNames.isEmpty()) {
            return Stream.empty();
        }

        // establish the arguments that will be resolved.
        final var arguments = new ArrayList<String>(2);

        arguments.add("--add-modules");

        // create the module path from the paths by concatenating them, each separated by the File.separator
        // ensure the module path elements are quoted (if necessary)
        final var moduleNames = this.moduleNames.stream()
            .map(ModuleName::get)
            .reduce("", (left, right) -> left.isEmpty() ? right : left + "," + right);

        arguments.add(moduleNames);

        return arguments.stream();
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof AddModules other)) {
            return false;
        }
        return Objects.equals(this.moduleNames, other.moduleNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.moduleNames);
    }

    /**
     * Creates an {@link AddModules} given an array of {@link ModuleName}s.
     *
     * @param moduleNames the {@link ModuleName}s
     * @return a new {@link AddModules}
     */
    public static AddModules of(final ModuleName... moduleNames) {
        return moduleNames == null
            ? AddModules.empty()
            : new AddModules(Arrays.stream(moduleNames));
    }

    /**
     * Creates a {@link AddModules} given an array of {@link ModuleName} {@link Stream}s.
     *
     * @param streams the {@link Stream} of {@link ModuleName}s
     * @return a {@link AddModules}
     */
    @SafeVarargs
    public static AddModules of(final Stream<ModuleName>... streams) {
        return streams == null
            ? AddModules.empty()
            : new AddModules(Arrays.stream(streams)
                .filter(Objects::nonNull)
                .reduce(Stream::concat)
                .orElseGet(Stream::empty));
    }

    /**
     * Creates a {@link AddModules} given an array of {@link String}s.
     *
     * @param moduleNames the {@link String}s
     * @return a {@link AddModules}
     */
    public static AddModules of(final String... moduleNames) {
        return moduleNames == null
            ? AddModules.empty()
            : new AddModules(Arrays.stream(moduleNames)
                .filter(Objects::nonNull)
                .map(ModuleName::of));
    }

    /**
     * Creates a {@link AddModules} based on a {@link String} representation using a comma separator
     *
     * @param string the {@link String} representation of a {@link AddModules}
     * @return a {@link AddModules}
     */
    public static AddModules of(final String string) {
        return Strings.isEmpty(string)
            ? AddModules.empty()
            : new AddModules(Arrays.stream(string.trim().split(","))
                .filter(moduleName -> !moduleName.isBlank())
                .map(ModuleName::of));
    }

    /**
     * Creates a {@link AddModules} given a {@link Stream} of {@link String} module names.
     *
     * @param moduleNames the {@link Stream} of {@link String} module names
     * @return a {@link AddModules}
     */
    public static AddModules of(final Stream<String> moduleNames) {
        return moduleNames == null
            ? AddModules.empty()
            : AddModules.of(moduleNames
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(moduleName -> !moduleName.isBlank())
                .map(ModuleName::of));
    }

    /**
     * Creates an empty {@link AddModules}.
     *
     * @return an empty {@link AddModules}
     */
    public static AddModules empty() {
        return EMPTY;
    }

    /**
     * Obtains the {@link Optional}ly defined {@link AddModules} of this Virtual Machine.
     *
     * @return the {@link Optional} {@link AddModules}, or {@link Optional#empty()} if none are defined
     */
    public static Optional<AddModules> inherited() {
        return ManagementFactory.getRuntimeMXBean()
            .getInputArguments()
            .stream()
            .filter(argument -> argument.startsWith("--add-modules="))
            .findFirst()
            .map(argument -> argument.substring("--add-modules=".length()))
            .map(AddModules::of);
    }
}
