package build.spawn.platform.local.jdk;

import build.base.foundation.Exceptional;
import build.spawn.jdk.Architecture;
import build.spawn.jdk.JDK;
import build.spawn.jdk.OperatingSystem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JDKDetector#of(Path)} tagging a detected {@link JDK} with the {@link OperatingSystem}
 * and {@link Architecture} it was built for, as opposed to those of the host running detection.
 *
 * @author reed.vonredwitz
 * @since Jul-2026
 */
class JDKDetectorForeignPlatformTests {

    /**
     * Ensure a {@code release} file describing a platform other than the host is honored, so a foreign
     * JDK staged for cross-target {@code jlink}ing is tagged correctly rather than assumed to be host-native.
     */
    @Test
    void shouldTagJDKWithForeignPlatformFromReleaseFile(@TempDir final Path tempDir) throws IOException {
        stageJDKHome(tempDir, """
            JAVA_VERSION="21.0.1"
            OS_NAME="Mac OS X"
            OS_ARCH="aarch64"
            """);

        final Exceptional<JDK> result = JDKDetector.of(tempDir);

        assertThat(result.isPresent()).isTrue();

        final var jdk = result.orElseThrow();
        assertThat(jdk.operatingSystem()).isEqualTo(OperatingSystem.MAC);
        assertThat(jdk.architecture()).isEqualTo(Architecture.AARCH64);
    }

    /**
     * Ensure a {@code release} file without {@code OS_NAME}/{@code OS_ARCH} entries falls back to the
     * host's current {@link OperatingSystem} and {@link Architecture}, preserving prior behavior.
     */
    @Test
    void shouldFallBackToHostPlatformWhenReleaseFileOmitsOsInfo(@TempDir final Path tempDir) throws IOException {
        stageJDKHome(tempDir, """
            JAVA_VERSION="21.0.1"
            """);

        final Exceptional<JDK> result = JDKDetector.of(tempDir);

        assertThat(result.isPresent()).isTrue();

        final var jdk = result.orElseThrow();
        assertThat(jdk.operatingSystem()).isEqualTo(OperatingSystem.current());
        assertThat(jdk.architecture()).isEqualTo(Architecture.current());
    }

    /**
     * Ensure a staged Windows JDK — which only ships {@code bin/java.exe}, not {@code bin/java} — is
     * still recognized as a valid Java Home rather than rejected outright.
     */
    @Test
    void shouldDetectWindowsJDKWithOnlyJavaExe(@TempDir final Path tempDir) throws IOException {
        final var bin = tempDir.resolve("bin");
        Files.createDirectories(bin);
        Files.createFile(bin.resolve("java.exe"));
        Files.writeString(tempDir.resolve("release"), """
            JAVA_VERSION="21.0.1"
            OS_NAME="Windows Server 2022"
            OS_ARCH="x86_64"
            """);

        final Exceptional<JDK> result = JDKDetector.of(tempDir);

        assertThat(result.isPresent()).isTrue();

        final var jdk = result.orElseThrow();
        assertThat(jdk.operatingSystem()).isEqualTo(OperatingSystem.WINDOWS);
        assertThat(jdk.architecture()).isEqualTo(Architecture.X86_64);
    }

    /**
     * Stages a minimal, fake JDK home: a {@code bin/java} placeholder (so {@link JDKDetector#of(Path)}
     * accepts it as a JDK home) and the specified {@code release} file content.
     */
    private static void stageJDKHome(final Path home, final String releaseContent) throws IOException {
        final var bin = home.resolve("bin");
        Files.createDirectories(bin);
        Files.createFile(bin.resolve("java"));
        Files.writeString(home.resolve("release"), releaseContent);
    }
}
