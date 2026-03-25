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
     * <p>
     * BUG: The class is currently named {@code CompleteableFutures} (extra {@code e} after
     * {@code Complet}) instead of the standard Java spelling {@code CompletableFutures},
     * which matches {@link java.util.concurrent.CompletableFuture}.  Any caller that attempts
     * to import or reference the class by its correct name will receive a compile or runtime
     * error.
     */
    @Test
    void classNameShouldBeSpelledCorrectly() {
        // Class.forName will throw ClassNotFoundException because only "CompleteableFutures" exists
        assertThatCode(() -> Class.forName("build.spawn.docker.okhttp.CompletableFutures"))
            .doesNotThrowAnyException();
    }
}
