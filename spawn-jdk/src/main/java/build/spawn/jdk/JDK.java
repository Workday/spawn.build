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
     * Constructs a {@link JDK}.
     *
     * @param version the {@link JDKVersion}
     * @param home    the {@link JDKHome}
     */
    private JDK(final JDKVersion version, final JDKHome home) {
        this.version = Objects.requireNonNull(version, "The JDKVersion must not be null");
        this.home = Objects.requireNonNull(home, "The JDKHome must not be null");
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

    @Override
    public String toString() {
        return "JDK{version=" + this.version + ", home=" + this.home.path() + "}";
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
        return Objects.equals(this.version, other.version) && Objects.equals(this.home, other.home);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.version, this.home);
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
        return of(JDKVersion.current(), JDKHome.current());
    }

    /**
     * Creates a {@link JDK} based on the specified {@link JDKVersion} and {@link JDKHome}.
     *
     * @param version the {@link JDKVersion}
     * @param home    the {@link JDKHome}
     * @return a new {@link JDK}
     */
    public static JDK of(final JDKVersion version, final JDKHome home) {
        return new JDK(version, home);
    }
}
