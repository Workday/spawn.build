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
 * The CPU architecture a {@link JDK} was built for.
 *
 * @author reed.vonredwitz
 * @since Jul-2026
 */
public enum Architecture {

    /**
     * 64-bit x86 (aka {@code amd64}, {@code x86_64}).
     */
    X86_64,

    /**
     * 64-bit ARM (aka {@code aarch64}, {@code arm64}).
     */
    AARCH64,

    /**
     * An architecture that could not be recognized.
     */
    OTHER;

    /**
     * Determines the {@link Architecture} for the specified name, typically sourced from the
     * {@code OS_ARCH} entry of a JDK's {@code release} file or the {@code os.arch} system property.
     *
     * @param name the architecture name
     * @return the {@link Architecture}, or {@link #OTHER} if it can't be recognized
     */
    public static Architecture of(final String name) {
        final var lower = name.toLowerCase(Locale.ROOT);

        if (lower.equals("amd64") || lower.equals("x86_64")) {
            return X86_64;
        }
        if (lower.equals("aarch64") || lower.equals("arm64")) {
            return AARCH64;
        }

        return OTHER;
    }

    /**
     * Obtains the {@link Architecture} of the currently executing Virtual Machine, based on the
     * {@code os.arch} system property.
     *
     * @return the current {@link Architecture}
     */
    public static Architecture current() {
        return of(System.getProperty("os.arch", ""));
    }
}
