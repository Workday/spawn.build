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
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JDK}, in particular that {@link JDK#compareTo(JDK)} stays consistent with
 * {@link JDK#equals(Object)} so a {@code SortedSet} of {@link JDK}s doesn't silently drop entries
 * that share a version but were built for different target platforms.
 *
 * @author reed.vonredwitz
 * @since Jul-2026
 */
class JDKTests {

    private static JDK jdk(final String version, final OperatingSystem os, final Architecture arch, final String home) {
        return JDK.of(JDKVersion.of(version), JDKHome.of(home), os, arch);
    }

    @Test
    void compareTo_ordersPrimarilyByVersion() {
        final var older = jdk("21.0.1", OperatingSystem.LINUX, Architecture.X86_64, "/jdk21");
        final var newer = jdk("25.0.3", OperatingSystem.LINUX, Architecture.X86_64, "/jdk25");

        assertThat(older.compareTo(newer)).isNegative();
        assertThat(newer.compareTo(older)).isPositive();
    }

    @Test
    void compareTo_isConsistentWithEqualsForSameVersionDifferentPlatforms() {
        final var linux = jdk("25.0.3", OperatingSystem.LINUX, Architecture.X86_64, "/jdk25-linux");
        final var mac = jdk("25.0.3", OperatingSystem.MAC, Architecture.AARCH64, "/jdk25-mac");

        assertThat(linux).isNotEqualTo(mac);
        assertThat(linux.compareTo(mac)).isNotZero();
        assertThat(mac.compareTo(linux)).isNotZero();
    }

    @Test
    void sortedSet_retainsBothJDKsOfTheSameVersionOnDifferentPlatforms() {
        final var linux = jdk("25.0.3", OperatingSystem.LINUX, Architecture.X86_64, "/jdk25-linux");
        final var mac = jdk("25.0.3", OperatingSystem.MAC, Architecture.AARCH64, "/jdk25-mac");
        final var linuxAarch64 = jdk("25.0.3", OperatingSystem.LINUX, Architecture.AARCH64, "/jdk25-linux-aarch64");

        final Set<JDK> sorted = new TreeSet<>(Set.of(linux, mac, linuxAarch64));

        assertThat(sorted).containsExactlyInAnyOrder(linux, mac, linuxAarch64);
    }

    @Test
    void compareTo_isConsistentWithEqualsForSamePlatformDifferentHome() {
        final var jdk1 = jdk("25.0.3", OperatingSystem.LINUX, Architecture.X86_64, "/opt/jdk-a");
        final var jdk2 = jdk("25.0.3", OperatingSystem.LINUX, Architecture.X86_64, "/opt/jdk-b");

        assertThat(jdk1).isNotEqualTo(jdk2);
        assertThat(jdk1.compareTo(jdk2)).isNotZero();
    }
}
