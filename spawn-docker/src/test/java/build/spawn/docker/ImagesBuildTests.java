package build.spawn.docker;

import build.base.configuration.Configuration;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for the {@code build} default methods on {@link Images}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
class ImagesBuildTests {

    /**
     * Ensure that an {@link IOException} thrown by a {@link DockerContextBuilder} propagates
     * to the caller rather than being silently swallowed.
     * <p>
     * BUG: The default {@link Images#build(DockerContextBuilder, build.base.configuration.Option...)}
     * method catches {@link IOException} from {@link DockerContextBuilder#build()} and returns
     * {@code Optional.empty()} without logging or rethrowing, making failures invisible to callers.
     */
    @Test
    void buildShouldPropagateIOExceptionFromContextBuilder() {
        // a DockerContextBuilder whose build() always fails
        final var failingBuilder = new DockerContextBuilder() {
            @Override
            public Path build() throws IOException {
                throw new IOException("simulated Docker context build failure");
            }
        };

        // minimal Images implementation — the bug lives in the default build(DockerContextBuilder) method
        final Images images = new Images() {
            @Override
            public Optional<Image> get(final String nameOrId) {
                return Optional.empty();
            }

            @Override
            public Optional<Image> get(final String nameOrId, final Configuration configuration) {
                return Optional.empty();
            }

            @Override
            public Optional<Image> pull(final String nameOrId, final Configuration configuration) {
                return Optional.empty();
            }

            @Override
            public Optional<Image> pull(final String nameOrId) {
                return Optional.empty();
            }

            @Override
            public Optional<Image> build(final Path contextPath, final Configuration configuration) {
                return Optional.empty();
            }
        };

        // the IOException should propagate; currently it is caught and Optional.empty() returned silently
        assertThatThrownBy(() -> images.build(failingBuilder))
            .isInstanceOf(IOException.class)
            .hasMessage("simulated Docker context build failure");
    }
}
