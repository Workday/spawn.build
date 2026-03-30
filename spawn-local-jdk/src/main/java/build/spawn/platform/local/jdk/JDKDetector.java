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

import build.base.foundation.Exceptional;
import build.base.logging.Logger;
import build.base.option.JDKVersion;
import build.spawn.jdk.JDK;
import build.spawn.jdk.option.JDKHome;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Detects the locally installed, operational and available {@link JDK}s.
 *
 * @author brian.oliver
 * @since Nov-2019
 */
public interface JDKDetector {

    /**
     * Obtains a {@link Stream} of candidate {@link java.nio.file.Path}s for {@link JDK} installations,
     * based on OS-specific glob patterns — without launching any subprocesses to verify them.
     * <p>
     * This is the cheap, fast phase of detection. Callers that only need to know whether any
     * {@link JDK}s are likely present (e.g. capability checks) should prefer this over
     * {@link #detect()}, which reads each candidate's {@code release} metadata file.
     *
     * @return a {@link Stream} of candidate {@link java.nio.file.Path}s
     */
    default Stream<Path> paths() {
        return Stream.empty();
    }

    /**
     * Obtains a {@link Stream} of the available {@link JDK}s.
     *
     * @return a {@link Stream} of {@link JDK}s
     */
    Stream<JDK> detect();

    /**
     * Obtains a {@link Stream} of the {@link JDKDetector}s that are available.
     *
     * @return a {@link Stream} of {@link JDKDetector}s
     */
    static Stream<JDKDetector> stream() {

        // establish a ServiceLoader to discover JDKDetectors
        final var serviceLoader = ServiceLoader.load(JDKDetector.class);

        // establish a collection of JDK.Detectors
        final var detectors = new ArrayList<JDKDetector>();
        for (final var detector : serviceLoader) {
            detectors.add(detector);
        }

        return detectors.stream();
    }

    /**
     * Obtains the current {@link JDK} based on the Virtual Machine in which this method is executed.
     *
     * @return the current {@link JDK}
     * @see JDK#current()
     */
    static JDK current() {
        return JDK.current();
    }

    /**
     * Attempts to create a {@link JDK} for the specified {@link Path} to a Java Home by reading its
     * {@code release} metadata file.
     * <p>
     * Should the specified {@link Path} refer to a Java Runtime Environment (JRE), an attempt will be made to
     * locate the {@link JDK} based on the specified {@link Path}.
     *
     * @param path the {@link Path}
     * @return the {@link Exceptional} {@link JDK}, otherwise {@link Exceptional#empty()}
     */
    static Exceptional<JDK> of(final Path path) {
        final var LOGGER = Logger.get(JDKDetector.class);

        // use provided path as the basis of the JDK location
        Path home = path;

        // ensure the path exists
        if (!home.toFile().exists()) {
            return Exceptional.empty();
        }

        if (home.endsWith("jre")) {
            // when a JRE, we have to look for the parent (to find the JDK)
            home = home.getParent();
        }

        if (!home.resolve("bin/java").toFile().exists()) {
            LOGGER.warn("The JDK Home [{0}] does not contain bin/java", home);
            return Exceptional.empty();
        }

        // read the release file to determine the JDK version — no subprocess needed
        final Path releaseFile = home.resolve("release");
        if (!releaseFile.toFile().exists()) {
            LOGGER.warn("The JDK Home [{0}] does not contain a release file", home);
            return Exceptional.empty();
        }

        // a Pattern to match JAVA_VERSION="..." in the release file
        final var VERSION = Pattern.compile("JAVA_VERSION=\"(.+?)\"");

        try {
            final var releaseContent = Files.readString(releaseFile);
            final var matcher = VERSION.matcher(releaseContent);

            if (matcher.find()) {
                final var javaVersion = JDKVersion.of(matcher.group(1));
                final var javaHome = JDKHome.of(home.toString());

                return Exceptional.of(JDK.of(javaVersion, javaHome));
            }
            else {
                LOGGER.warn("Could not detect Java version from release file at [{0}]", releaseFile);
                return Exceptional.empty();
            }
        }
        catch (final IOException e) {
            LOGGER.warn("Failed to read release file at [{0}]", releaseFile, e);
            return Exceptional.ofException(e);
        }
    }

    /**
     * Attempts to detect the default {@link JDK} for the currently running virtual machine by reading its
     * {@code release} metadata file from {@code java.home}.
     *
     * @return the {@link Optional} default {@link JDK}, otherwise {@link Optional#empty()} if it can't be detected
     */
    static Optional<JDK> detectDefault() {
        return of(Path.of(System.getProperty("java.home"))).optional();
    }
}
