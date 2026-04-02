package build.spawn.jdk.option;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ClassPath}.
 *
 * @author brian.oliver
 * @since Oct-2018
 */
class ClassPathTests {

    /**
     * Ensure an empty {@link ClassPath} can be created.
     */
    @Test
    void shouldCreateEmptyClassPath() {
        final var classPath = ClassPath.empty();

        assertThat(classPath)
            .isEmpty();

        assertThat(classPath.size())
            .isEqualTo(0);
    }

    /**
     * Ensure a {@link ClassPath} can be created based on the Virtual Machine.
     */
    @Test
    void shouldCreateSystemClassPath() {
        final ClassPath classPath = ClassPath.inherited();

        // there's no way the current ClassPath can be empty
        assertThat(classPath)
            .isNotEmpty();

        // ensure the paths in the ClassPath are in the java class path
        final var systemClassPath = System.getProperty("java.class.path");

        assertThat(classPath.paths()
            .filter(path -> systemClassPath.contains(path.toString())))
            .hasSize(classPath.size());
    }

    /**
     * Ensure {@link ClassPath} treats {@link Path}s as a set.
     */
    @Test
    void shouldTreatClassPathAsAPathSet() {
        // obtain the same Path, multiple times so we have multiple different instances
        final Path path1 = ClassPath.inherited().stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Expected a path"));

        final Path path2 = ClassPath.inherited().stream()
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Expected a path"));

        // create a ClassPath using the same Path multiple times
        final ClassPath classPath = ClassPath.of(path1, path2, path1, path2);

        // we should only have a single path in the ClassPath
        assertThat(classPath.size())
            .isEqualTo(1);

        assertThat(classPath.paths()
            .findFirst())
            .contains(path1);
    }
}
