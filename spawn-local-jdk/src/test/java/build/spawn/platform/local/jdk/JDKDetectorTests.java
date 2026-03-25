package build.spawn.platform.local.jdk;

import build.spawn.jdk.JDK;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

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
}
