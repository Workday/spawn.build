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

import java.util.Locale;

/**
 * The operating system a {@link JDK} was built for.
 *
 * @author reed.vonredwitz
 * @since Jul-2026
 */
public enum OperatingSystem {

    /**
     * Linux.
     */
    LINUX,

    /**
     * macOS.
     */
    MAC,

    /**
     * Windows.
     */
    WINDOWS,

    /**
     * An operating system that could not be recognized.
     */
    OTHER;

    /**
     * Determines the {@link OperatingSystem} for the specified name, typically sourced from the
     * {@code OS_NAME} entry of a JDK's {@code release} file or the {@code os.name} system property.
     *
     * @param name the operating system name
     * @return the {@link OperatingSystem}, or {@link #OTHER} if it can't be recognized
     */
    public static OperatingSystem of(final String name) {
        final var lower = name.toLowerCase(Locale.ROOT);

        if (lower.contains("mac") || lower.contains("darwin")) {
            return MAC;
        }
        if (lower.contains("windows")) {
            return WINDOWS;
        }
        if (lower.contains("linux")) {
            return LINUX;
        }

        return OTHER;
    }

    /**
     * Obtains the {@link OperatingSystem} of the currently executing Virtual Machine, based on the
     * {@code os.name} system property.
     *
     * @return the current {@link OperatingSystem}
     */
    public static OperatingSystem current() {
        return of(System.getProperty("os.name", ""));
    }
}
