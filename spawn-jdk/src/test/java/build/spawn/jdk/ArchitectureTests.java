package build.spawn.jdk;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Architecture}.
 *
 * @author reed.vonredwitz
 * @since Jul-2026
 */
class ArchitectureTests {

    /**
     * Ensure the common x86-64 spellings are recognized.
     */
    @Test
    void shouldRecognizeX86_64() {
        assertThat(Architecture.of("amd64")).isEqualTo(Architecture.X86_64);
        assertThat(Architecture.of("x86_64")).isEqualTo(Architecture.X86_64);
    }

    /**
     * Ensure the common AArch64 spellings are recognized.
     */
    @Test
    void shouldRecognizeAarch64() {
        assertThat(Architecture.of("aarch64")).isEqualTo(Architecture.AARCH64);
        assertThat(Architecture.of("arm64")).isEqualTo(Architecture.AARCH64);
    }

    /**
     * Ensure an unrecognized architecture name falls back to {@link Architecture#OTHER}.
     */
    @Test
    void shouldFallBackToOtherForUnrecognizedName() {
        assertThat(Architecture.of("riscv64")).isEqualTo(Architecture.OTHER);
    }

    /**
     * Ensure matching is case-insensitive.
     */
    @Test
    void shouldMatchCaseInsensitively() {
        assertThat(Architecture.of("AMD64")).isEqualTo(Architecture.X86_64);
    }

    /**
     * Ensure {@link Architecture#current()} matches the {@code os.arch} system property.
     */
    @Test
    void currentShouldMatchOsArchSystemProperty() {
        assertThat(Architecture.current())
            .isEqualTo(Architecture.of(System.getProperty("os.arch", "")));
    }
}
