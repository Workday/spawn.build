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

import build.base.expression.compat.Processor;
import build.base.expression.compat.Variable;
import build.base.foundation.Exceptional;
import build.base.logging.Logger;
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
import java.util.Properties;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * A {@link JDKDetector} that uses the properties defined by the
 * "java.home.properties" resource as the basis of detecting the installed {@link JDK}s.
 *
 * @author brian.oliver
 * @since Nov-2019
 */
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
     * A cache of the detected {@link JDK}s, ordering them implicitly by {@link build.base.option.JDKVersion}.
     */
    private final AtomicReference<SortedSet<JDK>> jdks;

    /**
     * Constructs the {@link JDKHomeBasedPatternDetector} {@link JDKDetector}.
     */
    public JDKHomeBasedPatternDetector() {
        this.jdks = new AtomicReference<>();
    }

    @Override
    public Stream<Path> paths() {

        // establish the JEL processor to expand ${user.home} in glob patterns
        final var processor = Processor.create(Variable.of("user.home", System.getProperty("user.home", "")));

        try {
            // use the known jdk homes properties file as the basis of JavaHome locations
            final Properties knownJdkHomes = new Properties();

            // load the jdk homes
            final InputStream inputStream =
                ClassLoader.getSystemResourceAsStream(JAVA_HOME_PROPERTIES);

            knownJdkHomes.load(inputStream);

            // map each of the java homes into a path — no subprocess verification
            return knownJdkHomes.stringPropertyNames().stream()
                .filter(key -> {
                    // keys must have an @ prefix: <os-pattern>@<unique-name>
                    // keys without @ are excluded
                    final int at = key.indexOf('@');
                    if (at < 0) {
                        return false;
                    }
                    final var osPattern = key.substring(0, at);
                    return matchesOS(System.getProperty("os.name", ""), osPattern);
                })
                .map(knownJdkHomes::getProperty)
                .map(processor::replace)
                .flatMap(pattern -> {
                    try {
                        if (pattern.contains("*") || pattern.contains("?") || pattern.contains("[")
                            || pattern.contains("]") || pattern.contains("{") || pattern.contains("}")) {

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
                            } else {
                                final Path base = Paths.get(pattern.substring(0, lastPathSeparator));
                                final String glob = "glob:" + pattern;

                                // walk the tree from the base to find paths matching the glob
                                final PathMatcher pathMatcher = FileSystems.getDefault()
                                    .getPathMatcher(glob);

                                final ArrayList<Path> paths = new ArrayList<>();

                                // compute max walk depth from the pattern suffix — patterns without **
                                // can only match at a fixed depth, so there's no need to go deeper.
                                // +1 because Files.walkFileTree calls preVisitDirectory for depths
                                // 0..maxDepth-1 only; at exactly maxDepth, directories are delivered
                                // via visitFile and our preVisitDirectory check never fires.
                                final String patternSuffix = pattern.substring(lastPathSeparator);
                                final int maxDepth = patternSuffix.contains("**")
                                    ? Integer.MAX_VALUE
                                    : (int) patternSuffix.chars().filter(c -> c == '/').count() + 1;

                                // attempt to find paths matching the glob, iff the base path exists
                                if (base.toFile().exists()) {
                                    Files.walkFileTree(
                                        base,
                                        EnumSet.of(FileVisitOption.FOLLOW_LINKS),
                                        maxDepth,
                                        new SimpleFileVisitor<Path>() {
                                            @Override
                                            public FileVisitResult preVisitDirectory(final Path path,
                                                                                     final BasicFileAttributes attrs) {
                                                if (pathMatcher.matches(path)) {
                                                    paths.add(path);

                                                    return FileVisitResult.SKIP_SUBTREE;
                                                } else {
                                                    return FileVisitResult.CONTINUE;
                                                }
                                            }

                                            @Override
                                            public FileVisitResult visitFileFailed(final Path file,
                                                                                   final IOException exc)
                                                throws IOException {
                                                if (exc instanceof AccessDeniedException) {
                                                    LOG.debug(
                                                        "Access denied visiting [{0}], skipping", file);
                                                    return FileVisitResult.CONTINUE;
                                                }
                                                return super.visitFileFailed(file, exc);
                                            }
                                        });

                                    return paths.stream();
                                } else {
                                    LOG.debug(
                                        "Skipping path [{0}] for pattern [{1}] as the path does not exist",
                                        base, pattern);
                                    return Stream.empty();
                                }
                            }
                        } else {
                            return Stream.of(Paths.get(pattern));
                        }
                    } catch (final InvalidPathException e) {
                        LOG.debug("The path [{0}] is not a valid pattern", pattern);
                        return Stream.empty();
                    } catch (final IOException e) {
                        LOG.debug("Failed to visit a path [{0}]", pattern, e);
                        return Stream.empty();
                    }
                });
        } catch (final IOException e) {
            LOG.error("Failed to read {0}", JAVA_HOME_PROPERTIES, e);
            return Stream.empty();
        }
    }

    @Override
    public Stream<JDK> detect() {

        // detect the installed JDKs once
        this.jdks.getAndUpdate(jdk -> {
            if (jdk == null) {
                // we've yet to detect the installed JDKs

                // the currently detected JDKs
                final SortedSet<JDK> detected = new ConcurrentSkipListSet<>();

                paths()
                    .map(JDKDetector::of)
                    .filter(Exceptional::isPresent)
                    .map(Exceptional::orElseThrow)
                    .forEach(detected::add);

                return detected;
            } else {
                // use the previously detected JDKs
                return jdk;
            }
        });

        return this.jdks.get().stream();
    }

    /**
     * Determines if the specified OS pattern matches the provided OS name.
     * <p>
     * Known OS kind patterns: {@code mac}, {@code windows}, {@code unix}, {@code posix}, {@code unknown}.
     * Any other pattern is treated as a regular expression matched against the OS name (lower-case).
     *
     * @param osName  the OS name (typically from {@code System.getProperty("os.name")})
     * @param pattern the OS pattern from the properties file key prefix
     * @return {@code true} if the pattern matches the OS name
     */
    static boolean matchesOS(final String osName, final String pattern) {
        final String lower = osName.toLowerCase();
        return switch (pattern.toLowerCase()) {
            case "mac" -> lower.contains("mac");
            case "windows" -> lower.contains("windows");
            case "unix" -> lower.contains("linux") || lower.contains("freebsd") || lower.contains("openbsd");
            case "posix" -> lower.contains("sunos") || lower.contains("solaris")
                || lower.contains("hp-ux") || lower.contains("aix");
            case "unknown" -> false;
            default -> lower.matches(pattern.toLowerCase());
        };
    }
}
