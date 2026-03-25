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

import build.base.flow.CompletingSubscriber;
import build.base.logging.Logger;
import build.base.option.JDKVersion;
import build.spawn.application.Application;
import build.spawn.application.Console;
import build.spawn.application.option.Argument;
import build.spawn.application.option.StandardErrorSubscriber;
import build.spawn.jdk.JDK;
import build.spawn.jdk.option.JDKHome;
import build.spawn.platform.local.LocalMachine;

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
     * Attempts to create {@link JDK} based on the specified {@link Path} to Java Home by testing its availability
     * and that it's operational.
     * <p>
     * Should specified {@link Path} refer to a Java Runtime Environment (JRE), and attempt will be made to
     * locate the {@link JDK} based on the specified {@link Path}.
     *
     * @param path the {@link Path}
     * @return the {@link Optional} operational and available {@link JDK}, otherwise {@link Optional#empty()}
     */
    static Optional<JDK> of(final Path path) {
        final var LOGGER = Logger.get(JDKDetector.class);

        // use provided path as the basis of the JDK location
        Path home = path;

        // ensure the path exists
        if (!home.toFile().exists()) {
            return Optional.empty();
        }

        if (home.endsWith("jre")) {
            // when a JRE, we have to look for the parent (to find the JDK)
            home = home.getParent();
        }

        if (!home.resolve("bin/java").toFile().exists()) {
            LOGGER.warn("The JDK Home [{0}] does not contain bin/java", home);
            return Optional.empty();
        }

        // attempt to execute "java -version" on the LocalMachine to prove that it's installed and detect the version
        final var machine = LocalMachine.get();
        final var executable = home.resolve("bin/java").toString();

        // a Pattern to match the java version line output by java
        final var VERSION = Pattern.compile("(?:java|openjdk) version \"(.+?)\".*");
        final var subscriber = new CompletingSubscriber<String>();
        final var version = subscriber.when(line -> VERSION.matcher(line).matches());

        try (Application application = machine.launch(executable,
            Argument.of("-version"),
            StandardErrorSubscriber.of(subscriber))) {

            // wait for the application to terminate
            application.onExit()
                .join();

            // iff we detected a version can we create a JDK
            if (version.isDone() && !version.isCompletedExceptionally() && !version.isCancelled()) {
                // determine the captured JavaVersion
                final var matcher = VERSION.matcher(version.get());

                // ensure (again) that it's matched
                if (matcher.matches()) {
                    //establish the JDK based on the home and version
                    final var javaVersion = JDKVersion.of(matcher.group(1));
                    final var javaHome = JDKHome.of(home.toString());

                    return Optional.of(JDK.of(javaVersion, javaHome));
                }
            }
            else {
                LOGGER.warn(
                    "The version of the JDK at [{0}] could not be detected. This installation will be ignored.", home);
            }
        }
        catch (final Exception e) {
            LOGGER.warn("Failed to execute bin/java in [{0}]", home, e);
        }

        return Optional.empty();
    }

    /**
     * Attempts to detect the locally installed operating system default {@link JDK}, by executing
     * {@code java -XshowSettings:properties -version}
     *
     * @return the {@link Optional} local {@link JDK}, otherwise {@link Optional#empty()} if it can't be detected
     */
    static Optional<JDK> detectDefault() {
        final var LOGGER = Logger.get(JDKDetector.class);

        final var machine = LocalMachine.get();
        final var executable = "java";

        final var subscriber = new CompletingSubscriber<String>();

        final var HOME_PREFIX = "java.home = ";
        final var home = subscriber.when(line -> line.trim().startsWith(HOME_PREFIX), String::trim);

        final var VERSION_PREFIX = "java.version = ";
        final var version = subscriber.when(line -> line.trim().startsWith(VERSION_PREFIX), String::trim);

        try (Application application = machine.launch(
            executable,
            Argument.of("-XshowSettings:properties"),
            Argument.of("-version"),
            StandardErrorSubscriber.of(subscriber))) {

            application.onExit()
                .join();

            if (home.isDone() && !home.isCancelled() && !home.isCompletedExceptionally()) {
                // obtain the JDKHome from the captured output
                final var jdkHome = JDKHome.of(home.get().substring(HOME_PREFIX.length()));

                // should we have also captured the JDKVersion, we can use that to create the JDK
                if (version.isDone() && !version.isCancelled() && !version.isCompletedExceptionally()) {
                    // obtain the JDKVersion from the captured output
                    final var jdkVersion = JDKVersion.of(version.get().substring(VERSION_PREFIX.length()));

                    // use the JavaHome to detect the JDK
                    return Optional.of(JDK.of(jdkVersion, jdkHome));
                }
                else {
                    // attempt to use JDK to detect the JDK
                    return of(jdkHome.path());
                }
            }

            return Optional.empty();
        }
        catch (final Exception e) {
            LOGGER.error("Failed to determine the default JDK installation", e);

            return Optional.empty();
        }
    }
}
