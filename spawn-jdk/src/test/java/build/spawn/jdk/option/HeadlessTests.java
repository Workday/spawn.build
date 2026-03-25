package build.spawn.jdk.option;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Headless}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
class HeadlessTests {

    /**
     * Ensure {@link Headless#ENABLED} resolves to the expected headless system property token.
     */
    @Test
    void enabledShouldResolveToHeadlessSystemProperty() {
        assertThat(Headless.ENABLED.resolve(null, null))
            .containsExactly("-Djava.awt.headless=true");
    }

    /**
     * Ensure {@link Headless#DISABLED} resolves to an empty stream, adding no tokens to the
     * JVM command line.
     * <p>
     * BUG: {@link Headless#DISABLED} returns {@code Stream.of("")} — a stream containing a
     * single empty-string token — instead of {@code Stream.empty()}.  This causes a blank
     * argument to be injected into the JVM command line, which can confuse argument parsers.
     */
    @Test
    void disabledShouldResolveToNoTokens() {
        assertThat(Headless.DISABLED.resolve(null, null))
            .isEmpty();
    }
}
