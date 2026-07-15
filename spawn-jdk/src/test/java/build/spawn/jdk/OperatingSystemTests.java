package build.spawn.jdk;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link OperatingSystem}.
 *
 * @author reed.vonredwitz
 * @since Jul-2026
 */
class OperatingSystemTests {

    /**
     * Ensure Mac-flavored OS names are recognized.
     */
    @Test
    void shouldRecognizeMac() {
        assertThat(OperatingSystem.of("Mac OS X")).isEqualTo(OperatingSystem.MAC);
        assertThat(OperatingSystem.of("Darwin")).isEqualTo(OperatingSystem.MAC);
    }

    /**
     * Ensure Windows-flavored OS names are recognized.
     */
    @Test
    void shouldRecognizeWindows() {
        assertThat(OperatingSystem.of("Windows 11")).isEqualTo(OperatingSystem.WINDOWS);
    }

    /**
     * Ensure Linux-flavored OS names are recognized.
     */
    @Test
    void shouldRecognizeLinux() {
        assertThat(OperatingSystem.of("Linux")).isEqualTo(OperatingSystem.LINUX);
    }

    /**
     * Ensure an unrecognized OS name falls back to {@link OperatingSystem#OTHER}.
     */
    @Test
    void shouldFallBackToOtherForUnrecognizedName() {
        assertThat(OperatingSystem.of("SunOS")).isEqualTo(OperatingSystem.OTHER);
    }

    /**
     * Ensure matching is case-insensitive.
     */
    @Test
    void shouldMatchCaseInsensitively() {
        assertThat(OperatingSystem.of("LINUX")).isEqualTo(OperatingSystem.LINUX);
    }

    /**
     * Ensure {@link OperatingSystem#current()} matches the {@code os.name} system property.
     */
    @Test
    void currentShouldMatchOsNameSystemProperty() {
        assertThat(OperatingSystem.current())
            .isEqualTo(OperatingSystem.of(System.getProperty("os.name", "")));
    }
}
