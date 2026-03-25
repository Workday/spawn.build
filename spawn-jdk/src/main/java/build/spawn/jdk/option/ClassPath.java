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
 * An immutable {@link JDKOption} to represent the class-path of a {@link JDKApplication}.
 *
 * @author brian.oliver
 * @since Oct-2018
 */
public class ClassPath
    implements JDKOption, Tabular, PathSet {

    /**
     * The {@link Path}s making up the {@link ClassPath}.
     */
    private final LinkedHashSet<Path> paths;

    /**
     * An empty {@link ClassPath}.
     */
    private static final ClassPath EMPTY = new ClassPath(Stream.empty());

    /**
     * Constructs a {@link ClassPath} from a {@link Stream} of {@link Path}s.
     *
     * @param paths the {@link Path}s
     */
    private ClassPath(final Stream<Path> paths) {
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
     * Obtain a {@link Stream} of the {@link Path}s defined by the {@link ClassPath}.
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
            table.options().add(TableName.of("Class Path"));
            table.options().add(CellSeparator.of(" : "));
            return table;
        });
    }

    @Override
    public void tabularize(final Table table) {
        this.paths.forEach(path -> table.addRow(path.toString()));
    }

    @Override
    public Stream<String> resolve(final Platform platform,
                                  final ConfigurationBuilder options) {

        // establish the arguments that will be resolved.
        final var arguments = new ArrayList<String>(2);

        // include the java "-classpath" option
        arguments.add("-classpath");

        // create the classpath from the paths by concatenating them, each separated by the File.separator
        // ensure the classpath elements are quoted (if necessary)
        final var classPath = this.paths.stream()
            .map(Path::toString)
            .map(Strings::doubleQuoteIfContainsWhiteSpace)
            .reduce("", (left, right) -> left.isEmpty() ? right : left + File.pathSeparator + right);

        arguments.add(classPath);

        return arguments.stream();
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ClassPath other)) {
            return false;
        }
        return Objects.equals(this.paths, other.paths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.paths);
    }

    /**
     * Creates a {@link ClassPath} given an array of {@link Path}s.
     *
     * @param paths the {@link Path}s
     * @return a {@link ClassPath}
     */
    public static ClassPath of(final Path... paths) {
        return paths == null
            ? ClassPath.empty()
            : new ClassPath(Arrays.stream(paths));
    }

    /**
     * Creates a {@link ClassPath} given an array of {@link Path} {@link Stream}s.
     *
     * @param streams the {@link Stream} of {@link Path}s
     * @return a {@link ClassPath}
     */
    @SafeVarargs
    public static ClassPath of(final Stream<Path>... streams) {
        return streams == null
            ? ClassPath.empty()
            : new ClassPath(Arrays.stream(streams)
                .reduce(Stream::concat)
                .orElseGet(Stream::empty));
    }

    /**
     * Creates a {@link ClassPath} given an array of {@link PathSet}s.
     *
     * @param pathSets the {@link PathSet}s
     * @return a {@link ClassPath}
     */
    public static ClassPath of(final PathSet... pathSets) {
        return pathSets == null
            ? ClassPath.empty()
            : new ClassPath(Arrays.stream(pathSets)
                .map(PathSet::stream)
                .reduce(Stream::concat)
                .orElseGet(Stream::empty));
    }

    /**
     * Creates a {@link ClassPath} based on a {@link String} representation, using the file and path
     * separators of this Java Virtual Machine.
     *
     * @param string the {@link String} representation of a {@link ClassPath}
     * @return a {@link ClassPath}
     */
    public static ClassPath of(final String string) {
        return Strings.isEmpty(string)
            ? ClassPath.empty()
            : new ClassPath(Arrays.stream(string.split(File.pathSeparator)).map(Paths::get));
    }

    /**
     * Creates a {@link ClassPath} given a {@link Stream} of {@link Path}s.
     *
     * @param paths the {@link Path}s
     * @return a {@link ClassPath}
     */
    public static ClassPath of(final Stream<Path> paths) {
        return new ClassPath(paths);
    }

    /**
     * Creates an empty {@link ClassPath}.
     *
     * @return an empty {@link ClassPath}
     */
    public static ClassPath empty() {
        return EMPTY;
    }

    /**
     * Obtains the {@link ClassPath} of this Java Virtual Machine.
     *
     * @return the {@link ClassPath}
     */
    @Default
    public static ClassPath inherited() {
        return ClassPath.of(System.getProperty("java.class.path"));
    }
}
