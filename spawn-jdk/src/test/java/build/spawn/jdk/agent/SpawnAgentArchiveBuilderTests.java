package build.spawn.jdk.agent;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link SpawnAgentArchiveBuilder}.
 *
 * @author brian.oliver
 * @since Jan-2025
 */
class SpawnAgentArchiveBuilderTests {

    /**
     * Ensure the {@link SpawnAgentArchiveBuilder} can create the {@link SpawnAgent} {@code jar} archive.
     */
    @Test
    void shouldCreateArchive() {

        // create the SpawnAgent archive
        final Path path = SpawnAgentArchiveBuilder.createArchive();

        assertThat(path)
            .isNotNull();

        assertThat(Files.exists(path))
            .isTrue();
    }
}
