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
import build.base.foundation.Strings;
import build.base.io.PathSet;
import build.base.option.JDKVersion;
import build.base.table.Table;
import build.base.table.Tabular;
import build.base.table.option.CellSeparator;
import build.base.table.option.TableName;
import build.spawn.application.Platform;
import build.spawn.jdk.JDKApplication;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * An immutable {@link JDKOption} to represent the module-path of a {@link JDKApplication}.
 *
 * @author brian.oliver
 * @since Jan-2025
 */
public class ModulePath
    implements JDKOption, Tabular, PathSet {

    /**
     * The {@link Path}s making up the {@link ModulePath}.
     */
    private final LinkedHashSet<Path> paths;

    /**
     * An empty {@link ModulePath}.
     */
    private static final ModulePath EMPTY = new ModulePath(Stream.empty());

    /**
     * Constructs a {@link ModulePath} from a {@link Stream} of {@link Path}s.
     *
     * @param paths the {@link Path}s
     */
    private ModulePath(final Stream<Path> paths) {
        this.paths = new LinkedHashSet<>();
        if (paths != null) {
            paths.filter(Objects::nonNull)
                .forEach(this.paths::add);
        }
    }

    @Override
    public boolean isEmpty() {
        return this.paths.isEmpty();
    }

    @Override
    public int size() {
        return this.paths.size();
    }

    @Override
    public Stream<Path> stream() {
        return this.paths.stream();
    }

    /**
     * Obtain a {@link Stream} of the {@link Path}s defined by the {@link ModulePath}.
     *
     * @return a {@link Stream} of {@link Path}s.
     */
    public Stream<Path> paths() {
        return stream();
    }

    @Override
    public boolean hasTabularContent() {
        return !this.paths.isEmpty();
    }

    @Override
    public Optional<Supplier<Table>> getTableSupplier() {
        return Optional.of(() -> {
            final Table table = Table.create();
            table.options().add(TableName.of("Module Path"));
            table.options().add(CellSeparator.of(" : "));
            return table;
        });
    }

    @Override
    public void tabularize(final Table table) {
        this.paths.forEach(path -> table.addRow(path.toString()));
    }

    @Override
    public boolean isSupported(final JDKVersion jdkVersion,
                               final ConfigurationBuilder options) {

        return jdkVersion.isModular();
    }

    @Override
    public Stream<String> resolve(final Platform platform,
                                  final ConfigurationBuilder options) {

        // don't attempt to add the Module Paths when there's no paths
        if (this.paths.isEmpty()) {
            return Stream.empty();
        }

        // establish the arguments that will be resolved.
        final var arguments = new ArrayList<String>(2);

        arguments.add("-p");

        // create the module path from the paths by concatenating them, each separated by the File.separator
        // ensure the module path elements are quoted (if necessary)
        final var modulePath = this.paths.stream()
            .map(Path::toString)
            .map(Strings::doubleQuoteIfContainsWhiteSpace)
            .reduce("", (left, right) -> left.isEmpty() ? right : left + File.pathSeparator + right);

        arguments.add(modulePath);

        return arguments.stream();
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ModulePath other)) {
            return false;
        }
        return Objects.equals(this.paths, other.paths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.paths);
    }

    /**
     * Creates a {@link ModulePath} given an array of {@link Path}s.
     *
     * @param paths the {@link Path}s
     * @return a {@link ModulePath}
     */
    public static ModulePath of(final Path... paths) {
        return paths == null
            ? ModulePath.empty()
            : new ModulePath(Arrays.stream(paths));
    }

    /**
     * Creates a {@link ModulePath} given an array of {@link Path} {@link Stream}s.
     *
     * @param streams the {@link Stream} of {@link Path}s
     * @return a {@link ModulePath}
     */
    @SafeVarargs
    public static ModulePath of(final Stream<Path>... streams) {
        return streams == null
            ? ModulePath.empty()
            : new ModulePath(Arrays.stream(streams)
                .reduce(Stream::concat)
                .orElseGet(Stream::empty));
    }

    /**
     * Creates a {@link ModulePath} given an array of {@link PathSet}s.
     *
     * @param pathSets the {@link PathSet}s
     * @return a {@link ModulePath}
     */
    public static ModulePath of(final PathSet... pathSets) {
        return pathSets == null
            ? ModulePath.empty()
            : new ModulePath(Arrays.stream(pathSets)
                .map(PathSet::stream)
                .reduce(Stream::concat)
                .orElseGet(Stream::empty));
    }

    /**
     * Creates a {@link ModulePath} based on a {@link String} representation, using the file and path
     * separators of this Java Virtual Machine.
     *
     * @param string the {@link String} representation of a {@link ModulePath}
     * @return a {@link ModulePath}
     */
    public static ModulePath of(final String string) {
        return Strings.isEmpty(string)
            ? ModulePath.empty()
            : new ModulePath(Arrays.stream(string.split(File.pathSeparator)).map(Paths::get));
    }

    /**
     * Creates a {@link ModulePath} given a {@link Stream} of {@link Path}s.
     *
     * @param paths the {@link Path}s
     * @return a {@link ModulePath}
     */
    public static ModulePath of(final Stream<Path> paths) {
        return new ModulePath(paths);
    }

    /**
     * Creates an empty {@link ModulePath}.
     *
     * @return an empty {@link ModulePath}
     */
    public static ModulePath empty() {
        return EMPTY;
    }

    /**
     * Obtains the {@link Optional}ly defined {@link ModulePath} of this Virtual Machine.
     *
     * @return the {@link Optional} {@link ModulePath} or {@link Optional#empty()} if one is not defined
     */
    @Default
    public static Optional<ModulePath> inherited() {
        final var modulePath = System.getProperty("jdk.module.path");

        return Strings.isEmpty(modulePath)
            ? Optional.empty()
            : Optional.of(ModulePath.of(modulePath));
    }
}
