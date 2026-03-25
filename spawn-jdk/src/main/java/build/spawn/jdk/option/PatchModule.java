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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A {@link JDKOption} to specify a {@code --patch-module=module/path}.
 *
 * @author brian.oliver
 * @since Jan-2025
 */
public class PatchModule
    implements JDKOption, CollectedOption<List>, Tabular {

    /**
     * The {@link ModuleName} to be patched.
     */
    private final ModuleName moduleName;

    /**
     * The {@link Path} containing the file or folder contents for the patch.
     */
    private final Path path;

    /**
     * Constructs a {@link PatchModule}.
     *
     * @param moduleName the {@link ModuleName}
     * @param path       the {@link Path} containing the patch content
     */
    private PatchModule(final ModuleName moduleName, final Path path) {
        this.moduleName = Objects.requireNonNull(moduleName, "The ModuleName must not be null");
        this.path = Objects.requireNonNull(path, "The Path must not be null");
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

        arguments.add("--patch-module");
        arguments.add(this.moduleName.get() + "=" + this.path);

        return arguments.stream();
    }

    @Override
    public Optional<Supplier<Table>> getTableSupplier() {
        return Optional.of(() -> {
            final Table table = Table.create();
            table.options().add(TableName.of("Patch Module"));
            table.options().add(CellSeparator.of("="));
            return table;
        });
    }

    @Override
    public void tabularize(final Table table) {
        table.addRow(Cell.of(this.moduleName.get()), Cell.of(this.path.toString()));
    }

    @Override
    public String toString() {
        return "PatchModule{moduleName=" + moduleName + ", path=" + path + '}';
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof final PatchModule that)) {
            return false;
        }
        return Objects.equals(this.moduleName, that.moduleName) && Objects.equals(this.path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.moduleName, this.path);
    }

    /**
     * Creates a {@link PatchModule}.
     *
     * @param moduleName the {@link ModuleName}
     * @param path       the {@link Path} containing the patch content
     */
    public static PatchModule of(final ModuleName moduleName, final Path path) {
        return new PatchModule(moduleName, path);
    }

    /**
     * Creates a {@link PatchModule}.
     *
     * @param moduleName the {@link ModuleName}
     * @param path       the {@link Path} containing the patch content
     */
    public static PatchModule of(final String moduleName, final String path) {
        return new PatchModule(ModuleName.of(moduleName), Path.of(path));
    }

    /**
     * Detects the {@link Stream} of {@link PatchModule}s for this Virtual Machine.
     *
     * @return the {@link Stream} of {@link PatchModule}
     */
    public static Stream<PatchModule> detect() {
        return ManagementFactory.getRuntimeMXBean()
            .getInputArguments()
            .stream()
            .filter(argument -> argument.startsWith("--patch-module="))
            .map(argument -> argument.substring("--patch-module=".length()))
            .map(argument -> argument.split("="))
            .map(array -> PatchModule.of(array[0], array[1]));
    }
}
