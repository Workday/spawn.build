package build.spawn.docker.okhttp.command;

import build.base.configuration.Configuration;
import build.base.io.Terminal;
import build.spawn.docker.Container;
import build.spawn.docker.Executable;
import build.spawn.docker.Image;
import build.spawn.docker.option.Command;
import build.spawn.docker.option.KillSignal;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the HTTP request built by {@link KillContainer}.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
class KillContainerRequestTests {

    /**
     * A minimal {@link Container} stub that returns a fixed id and throws
     * {@link UnsupportedOperationException} for all other methods.
     */
    private static final class StubContainer
        implements Container {

        private final String id;

        StubContainer(final String id) {
            this.id = id;
        }

        @Override
        public String id() {
            return this.id;
        }

        @Override
        public Image image() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Configuration configuration() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletableFuture<Container> onStart() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletableFuture<Container> onExit() {
            throw new UnsupportedOperationException();
        }

        @Override
        public OptionalInt exitValue() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Terminal attach(final Configuration configuration) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Executable createExecutable(final Command command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove(final Configuration configuration) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void copyFiles(final Path archivePath,
                              final String destinationDirectory,
                              final Path... filesToCopy) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Map<String, String>> fileInformation(final Path filePath) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void stop(final Configuration configuration) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void kill(final Configuration configuration) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletableFuture<Container> pause() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletableFuture<Container> unpause() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Information> inspect() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Injects a {@link StubContainer} into the {@link KillContainer}'s {@code container} field.
     *
     * @param killCommand the command to inject into
     * @param containerId the container id the stub should return
     * @throws Exception if reflection fails
     */
    private static void injectContainer(final KillContainer killCommand, final String containerId)
        throws Exception {

        final Field field = KillContainer.class.getDeclaredField("container");
        field.setAccessible(true);
        field.set(killCommand, new StubContainer(containerId));
    }

    /**
     * Ensure the kill request includes a {@code signal} query parameter matching the
     * {@link KillSignal} in the configuration.
     */
    @Test
    void requestShouldIncludeConfiguredSignal() throws Exception {
        final var command = new KillContainer(Configuration.of(KillSignal.SIGTERM));

        injectContainer(command, "abc123");

        final var url = HttpUrl.parse("http://localhost/");
        final var request = command.createRequest(url.newBuilder());

        assertThat(request.url().queryParameter("signal")).isEqualTo("SIGTERM");
    }

    /**
     * Ensure the default signal is {@code SIGKILL} when no {@link KillSignal} is configured,
     * preserving Docker's default kill behaviour.
     */
    @Test
    void requestShouldDefaultToSigkill() throws Exception {
        final var command = new KillContainer(Configuration.empty());

        injectContainer(command, "abc123");

        final var url = HttpUrl.parse("http://localhost/");
        final var request = command.createRequest(url.newBuilder());

        assertThat(request.url().queryParameter("signal")).isEqualTo("SIGKILL");
    }
}
