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
     * When the JVM is launched purely on the module path (e.g. under spin), {@code java.class.path}
     * is empty and {@link ClassPath#inherited()} correctly returns an empty {@link ClassPath}.
     */
    @Test
    void shouldCreateSystemClassPath() {
        final ClassPath classPath = ClassPath.inherited();

        // ClassPath.inherited() must exactly reflect java.class.path — empty or not
        final var systemClassPath = System.getProperty("java.class.path", "");

        if (systemClassPath.isEmpty()) {
            assertThat(classPath)
                .isEmpty();
        } else {
            assertThat(classPath)
                .isNotEmpty();

            assertThat(classPath.paths()
                .filter(path -> systemClassPath.contains(path.toString())))
                .hasSize(classPath.size());
        }
    }

    /**
     * Ensure {@link ClassPath} treats {@link Path}s as a set.
     */
    @Test
    void shouldTreatClassPathAsAPathSet() {
        // use a stable, always-present path rather than the system ClassPath
        // (which may be empty when the JVM is launched on the module path only)
        final Path path1 = Path.of(System.getProperty("user.home"));
        final Path path2 = Path.of(System.getProperty("user.home"));

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
