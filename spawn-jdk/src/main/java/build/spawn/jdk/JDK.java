package build.spawn.jdk;

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

import build.base.option.JDKVersion;
import build.spawn.jdk.option.JDKHome;

import java.util.Objects;

/**
 * Represents information concerning the installation of a JDK (not JRE).
 *
 * @author brian.oliver
 * @since Nov-2019
 */
public final class JDK
    implements Comparable<JDK> {

    /**
     * The {@link JDKVersion}.
     */
    private final JDKVersion version;

    /**
     * The {@link JDKHome}.
     */
    private final JDKHome home;

    /**
     * The {@link OperatingSystem} the {@link JDK} was built for.
     */
    private final OperatingSystem operatingSystem;

    /**
     * The {@link Architecture} the {@link JDK} was built for.
     */
    private final Architecture architecture;

    /**
     * Constructs a {@link JDK}.
     *
     * @param version         the {@link JDKVersion}
     * @param home            the {@link JDKHome}
     * @param operatingSystem the {@link OperatingSystem} the {@link JDK} was built for
     * @param architecture    the {@link Architecture} the {@link JDK} was built for
     */
    private JDK(final JDKVersion version, final JDKHome home, final OperatingSystem operatingSystem,
                final Architecture architecture) {
        this.version = Objects.requireNonNull(version, "The JDKVersion must not be null");
        this.home = Objects.requireNonNull(home, "The JDKHome must not be null");
        this.operatingSystem = Objects.requireNonNull(operatingSystem, "The OperatingSystem must not be null");
        this.architecture = Objects.requireNonNull(architecture, "The Architecture must not be null");
    }

    /**
     * Obtains the {@link JDKVersion} of the {@link JDK}.
     *
     * @return the {@link JDKVersion}
     */
    public JDKVersion version() {
        return this.version;
    }

    /**
     * Obtains the {@link JDKHome} of the {@link JDK}.
     *
     * @return the {@link JDKHome}
     */
    public JDKHome home() {
        return this.home;
    }

    /**
     * Obtains the {@link OperatingSystem} the {@link JDK} was built for.
     *
     * @return the {@link OperatingSystem}
     */
    public OperatingSystem operatingSystem() {
        return this.operatingSystem;
    }

    /**
     * Obtains the {@link Architecture} the {@link JDK} was built for.
     *
     * @return the {@link Architecture}
     */
    public Architecture architecture() {
        return this.architecture;
    }

    @Override
    public String toString() {
        return "JDK{version=" + this.version + ", home=" + this.home.path()
            + ", os=" + this.operatingSystem + ", arch=" + this.architecture + "}";
    }

    @Override
    public int compareTo(final JDK other) {
        return version().compareTo(other.version());
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof final JDK other)) {
            return false;
        }
        return Objects.equals(this.version, other.version) && Objects.equals(this.home, other.home)
            && this.operatingSystem == other.operatingSystem && this.architecture == other.architecture;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.version, this.home, this.operatingSystem, this.architecture);
    }

    /**
     * Obtains the current {@link JDK}, based on the "java.version" and "java.home" system properties of the currently
     * executing Virtual Machine.
     * <p>
     * NOTE: This may be different to the "default" {@link JDK} that is defined by the operating system,
     * as this Virtual Machine may have be started with a different {@link JDK}.
     *
     * @return the current {@link JDK}
     */
    public static JDK current() {
        return of(JDKVersion.current(), JDKHome.current(), OperatingSystem.current(), Architecture.current());
    }

    /**
     * Creates a {@link JDK} based on the specified {@link JDKVersion} and {@link JDKHome}, assuming it was
     * built for the {@link OperatingSystem} and {@link Architecture} of the currently executing Virtual Machine.
     *
     * @param version the {@link JDKVersion}
     * @param home    the {@link JDKHome}
     * @return a new {@link JDK}
     */
    public static JDK of(final JDKVersion version, final JDKHome home) {
        return of(version, home, OperatingSystem.current(), Architecture.current());
    }

    /**
     * Creates a {@link JDK} based on the specified {@link JDKVersion}, {@link JDKHome}, {@link OperatingSystem}
     * and {@link Architecture}.
     *
     * @param version         the {@link JDKVersion}
     * @param home            the {@link JDKHome}
     * @param operatingSystem the {@link OperatingSystem} the {@link JDK} was built for
     * @param architecture    the {@link Architecture} the {@link JDK} was built for
     * @return a new {@link JDK}
     */
    public static JDK of(final JDKVersion version, final JDKHome home, final OperatingSystem operatingSystem,
                          final Architecture architecture) {
        return new JDK(version, home, operatingSystem, architecture);
    }
}
