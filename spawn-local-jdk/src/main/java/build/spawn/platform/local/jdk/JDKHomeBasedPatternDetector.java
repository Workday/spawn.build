package build.spawn.platform.local.jdk;

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

import build.base.logging.Logger;
import build.base.option.JDKVersion;
import build.spawn.jdk.JDK;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Properties;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import com.google.auto.service.AutoService;

/**
 * A {@link JDKDetector} that uses the properties defined by the
 * "java.home.properties" resource as the basis of detecting the installed {@link JDK}s.
 *
 * @author brian.oliver
 * @since Nov-2019
 */
@AutoService(JDKDetector.class)
public class JDKHomeBasedPatternDetector
    implements JDKDetector {

    /**
     * The {@link Logger}.
     */
    private static final Logger LOG = Logger.get(JDKHomeBasedPatternDetector.class);

    /**
     * The "java.home.properties" resource.
     */
    private static final String JAVA_HOME_PROPERTIES = "java.home.properties";

    /**
     * A cache of the detected {@link JDK}s, ordering them implicitly by {@link JDKVersion}.
     */
    private final AtomicReference<SortedSet<JDK>> jdks;

    /**
     * Constructs the {@link JDKHomeBasedPatternDetector} {@link JDKDetector}.
     */
    public JDKHomeBasedPatternDetector() {
        this.jdks = new AtomicReference<>();
    }

    @Override
    public Stream<JDK> detect() {

        // detect the installed JDKs once
        this.jdks.getAndUpdate(jdk -> {
            if (jdk == null) {
                // we've yet to detect the installed JDKs

                // the currently detected JDKs
                // (we perform detection concurrently, so it has to be thread-safe)
                final SortedSet<JDK> detected = new ConcurrentSkipListSet<>();

                try {
                    // use the known jdk homes properties file as the basis of JavaHome locations
                    final Properties knownJdkHomes = new Properties();

                    // load the jdk homes
                    final InputStream inputStream =
                        ClassLoader.getSystemResourceAsStream(JAVA_HOME_PROPERTIES);

                    knownJdkHomes.load(inputStream);

                    // map each of the java homes into a path and determine the JDK from the path
                    knownJdkHomes.stringPropertyNames().stream()
                        .map(knownJdkHomes::getProperty)
                        .flatMap(pattern -> {
                            try {
                                if (pattern.contains("*") || pattern.contains("?") || pattern.contains("[")
                                    || pattern.contains("]") || pattern.contains("{") || pattern.contains(
                                    "}")) {

                                    // find the position of the last file.separator (/) before a glob pattern
                                    // this will be the base path and the rest is the glob pattern
                                    int i = 0;
                                    int lastPathSeparator = -1;
                                    while (i < pattern.length() && pattern.charAt(i) != '*'
                                        && pattern.charAt(i) != '?' && pattern.charAt(i) != '['
                                        && pattern.charAt(i) != ']' && pattern.charAt(i) != '{'
                                        && pattern.charAt(i) != '}') {

                                        if (pattern.charAt(i) == '/') {
                                            lastPathSeparator = i;
                                        }

                                        i++;
                                    }

                                    if (lastPathSeparator < 0) {
                                        LOG.warn("The path [{0}] is not an absolute path", pattern);
                                        return Stream.empty();
                                    }
                                    else {
                                        final Path base = Paths.get(pattern.substring(0, lastPathSeparator));
                                        final String glob = "glob:" + pattern;

                                        // walk the tree from the base to find paths matching the glob
                                        final PathMatcher pathMatcher = FileSystems.getDefault()
                                            .getPathMatcher(glob);

                                        final ArrayList<Path> paths = new ArrayList<>();

                                        // attempt to find paths matching the glob, iff the base path exists
                                        if (base.toFile().exists()) {
                                            Files.walkFileTree(
                                                base,
                                                EnumSet.of(FileVisitOption.FOLLOW_LINKS),
                                                Integer.MAX_VALUE,
                                                new SimpleFileVisitor<Path>() {
                                                    @Override
                                                    public FileVisitResult preVisitDirectory(final Path path,
                                                                                             final BasicFileAttributes attrs) {
                                                        if (pathMatcher.matches(path)) {
                                                            paths.add(path);

                                                            return FileVisitResult.SKIP_SUBTREE;
                                                        }
                                                        else {
                                                            return FileVisitResult.CONTINUE;
                                                        }
                                                    }

                                                    @Override
                                                    public FileVisitResult visitFileFailed(final Path file,
                                                                                           final IOException exc)
                                                        throws IOException {
                                                        if (exc instanceof AccessDeniedException) {
                                                            LOG.debug(
                                                                "Failed to access a file: " + file.toString(),
                                                                exc);
                                                            return FileVisitResult.CONTINUE;
                                                        }
                                                        return super.visitFileFailed(file, exc);
                                                    }
                                                });

                                            return paths.stream();
                                        }
                                        else {
                                            LOG.debug(
                                                "Skipping path [{0}] for pattern [{1}] as the path does not exist",
                                                base, pattern);
                                            return Stream.empty();
                                        }
                                    }
                                }
                                else {
                                    return Stream.of(Paths.get(pattern));
                                }
                            }
                            catch (final InvalidPathException e) {
                                LOG.debug("The path [{0}] is not a valid pattern", pattern);
                                return Stream.empty();
                            }
                            catch (final IOException e) {
                                LOG.debug("Failed to visit a path [{0}]", pattern, e);
                                return Stream.empty();
                            }
                        })
                        .parallel()
                        .map(JDKDetector::of)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .forEach(detected::add);
                }
                catch (final IOException e) {
                    LOG.error("Failed to read {0}", JAVA_HOME_PROPERTIES, e);
                }

                return detected;
            }
            else {
                // use the previously detected JDKs
                return jdk;
            }
        });

        return this.jdks.get().stream();
    }
}
