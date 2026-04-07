package build.spawn.docker.okhttp;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Tests that verify the naming of utility classes in {@code build.spawn.docker.okhttp}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
class CompletableFuturesNamingTests {

    /**
     * Ensure the {@code CompletableFutures} utility class is spelled correctly.
     */
    @Test
    void classNameShouldBeSpelledCorrectly() {
        assertThatCode(() -> Class.forName("build.spawn.docker.okhttp.CompletableFutures"))
            .doesNotThrowAnyException();
    }
}
