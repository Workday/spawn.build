package build.spawn.jdk.option;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ModulePath}.
 *
 * @author brian.oliver
 * @since Jan-2025
 */
class ModulePathTests {

    /**
     * Ensure an empty {@link ModulePath} can be created.
     */
    @Test
    void shouldCreateEmptyModulePath() {
        final var modulePath = ModulePath.empty();

        assertThat(modulePath)
            .isEmpty();

        assertThat(modulePath.size())
            .isEqualTo(0);
    }

    /**
     * Ensure a {@link ModulePath} can be created based on the Virtual Machine.
     */
    @Test
    void shouldCreateSystemModulePath() {
        assumeThat(ModulePath.inherited())
            .isNotEmpty();

        final var modulePath = ModulePath.inherited()
            .orElseThrow();

        // there's no way the current ModulePath can be empty
        assertThat(modulePath)
            .isNotEmpty();

        // ensure the paths in the ModulePath are in the path
        final var systemModulePath = System.getProperty("jdk.module.path");

        assertThat(modulePath.paths()
            .filter(path -> systemModulePath.contains(path.toString())))
            .hasSize(modulePath.size());
    }

    /**
     * Ensure {@link ModulePath} treats {@link Path}s as a set.
     */
    @Test
    void shouldTreatModulePathAsAPathSet() {
        assumeThat(ModulePath.inherited())
            .isNotEmpty();

        // obtain the same Path, multiple times so we have multiple different instances
        final Path path1 = ModulePath.inherited()
            .orElseThrow()
            .stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Expected a path"));

        final Path path2 = ModulePath.inherited()
            .orElseThrow()
            .stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Expected a path"));

        // create a ModulePath using the same Path multiple times
        final ModulePath modulePath = ModulePath.of(path1, path2, path1, path2);

        // we should only have a single path in the ModulePath
        assertThat(modulePath.size())
            .isEqualTo(1);

        assertThat(modulePath.paths()
            .findFirst())
            .contains(path1);
    }
}
