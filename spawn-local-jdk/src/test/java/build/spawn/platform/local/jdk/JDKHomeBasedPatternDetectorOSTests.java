package build.spawn.platform.local.jdk;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for OS pattern matching in {@link JDKHomeBasedPatternDetector}.
 *
 * @author brian.oliver
 * @since Mar-2026
 */
class JDKHomeBasedPatternDetectorOSTests {

    /**
     * Ensure the {@code mac} pattern matches Mac OS X.
     */
    @Test
    void shouldMatchMacPattern() {
        assertThat(JDKHomeBasedPatternDetector.matchesOS("Mac OS X", "mac"))
            .isTrue();
    }

    /**
     * Ensure the {@code windows} pattern matches Windows 10.
     */
    @Test
    void shouldMatchWindowsPattern() {
        assertThat(JDKHomeBasedPatternDetector.matchesOS("Windows 10", "windows"))
            .isTrue();
    }

    /**
     * Ensure the {@code unix} pattern matches Linux.
     */
    @Test
    void shouldMatchUnixPatternForLinux() {
        assertThat(JDKHomeBasedPatternDetector.matchesOS("Linux", "unix"))
            .isTrue();
    }

    /**
     * Ensure the {@code unix} pattern matches FreeBSD.
     */
    @Test
    void shouldMatchUnixPatternForFreeBSD() {
        assertThat(JDKHomeBasedPatternDetector.matchesOS("FreeBSD", "unix")).
            isTrue();
    }

    /**
     * Ensure the {@code posix} pattern matches SunOS.
     */
    @Test
    void shouldMatchPosixPatternForSolaris() {
        assertThat(JDKHomeBasedPatternDetector.matchesOS("SunOS", "posix"))
            .isTrue();
    }

    /**
     * Ensure the {@code mac} pattern does not match Linux.
     */
    @Test
    void shouldNotMatchMacPatternForLinux() {
        assertThat(JDKHomeBasedPatternDetector.matchesOS("Linux", "mac"))
            .isFalse();
    }

    /**
     * Ensure the {@code windows} pattern does not match Mac OS X.
     */
    @Test
    void shouldNotMatchWindowsPatternForMac() {
        assertThat(JDKHomeBasedPatternDetector.matchesOS("Mac OS X", "windows"))
            .isFalse();
    }

    /**
     * Ensure a regex pattern is matched against the OS name.
     */
    @Test
    void shouldMatchRegexPattern() {
        assertThat(JDKHomeBasedPatternDetector.matchesOS("Mac OS X", "mac.*"))
            .isTrue();
    }
}
