package build.spawn.platform.local.jdk;

import build.base.foundation.Exceptional;
import build.spawn.jdk.JDK;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

/**
 * Tests for {@link JDKDetector}s.
 *
 * @author brian.oliver
 * @since Nov-2019
 */
class JDKDetectorTests {

    /**
     * Ensure the current {@link JDK} can be detected.
     */
    @Test
    void shouldDetectCurrentJDK() {
        final var jdk = JDKDetector.current();

        assertThat(jdk)
            .isNotNull();

        assertThat(jdk)
            .isEqualTo(JDK.current());

        System.out.println("Current " + jdk);
    }

    /**
     * Ensure the {@link JDKDetector} can detect {@link JDK}s.
     */
    @Test
    void shouldDetectInstalledJDKs() {

        assertThat(JDKDetector.stream()
            .flatMap(JDKDetector::detect)
            .peek(jdk -> System.out.println("Detected " + jdk)))
            .isNotEmpty();
    }

    /**
     * Ensure the local default {@link JDK} can be detected.
     */
    @Test
    void shouldDetectLocalDefaultJDK() {
        final var jdk = JDKDetector.detectDefault();

        assertThat(jdk)
            .isPresent();

        System.out.println("Detected Default JDK: " + jdk.get());
    }

    /**
     * Ensure {@link JDKDetector#paths()} returns the paths to the installed {@link JDK}s.
     */
    @Test
    void shouldReturnPathsForInstalledJDKs() {
        assertThat(JDKDetector.stream()
            .flatMap(JDKDetector::paths)
            .peek(path -> System.out.println("Path: " + path)))
            .isNotEmpty();
    }

    /**
     * Ensure {@link JDKDetector#of(Path)} returns a present {@link Exceptional} for a known-good JDK home.
     */
    @Test
    void shouldDetectJDKAtCurrentJavaHome() {
        final var home = Path.of(System.getProperty("java.home"));

        final Exceptional<JDK> result = JDKDetector.of(home);

        assertThat(result.isPresent()).isTrue();
        assertThat(result.orElseThrow().home().get()).isNotNull();
    }

    /**
     * Ensure {@link JDKDetector#of(Path)} returns an empty {@link Exceptional} for a path that does not exist.
     */
    @Test
    void shouldReturnEmptyExceptionalForNonExistentPath() {
        final Exceptional<JDK> result = JDKDetector.of(Path.of("/nonexistent/path/to/jdk"));

        assertThat(result.isPresent()).isFalse();
    }

    /**
     * Ensure {@link JDKDetector#of(Path)} returns an empty {@link Exceptional} for a directory that has no
     * {@code bin/java}.
     */
    @Test
    void shouldReturnEmptyExceptionalForDirectoryWithoutBinJava(@TempDir final Path tempDir) {
        final Exceptional<JDK> result = JDKDetector.of(tempDir);

        assertThat(result.isPresent()).isFalse();
    }
}
