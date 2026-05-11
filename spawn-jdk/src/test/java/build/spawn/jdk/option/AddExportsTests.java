package build.spawn.jdk.option;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AddExports}.
 *
 * @author reed.vonredwitz
 * @since May-2026
 */
class AddExportsTests {

    /**
     * Ensure {@link AddExports} resolves to the expected command-line tokens.
     */
    @Test
    void shouldResolveToExpectedTokens() {
        final var addExports = AddExports.of("com.example.app", "com.example.app.internal", "com.example.lib");

        assertThat(addExports.resolve(null, null))
            .containsExactly("--add-exports", "com.example.app/com.example.app.internal=com.example.lib");
    }

    /**
     * Ensure {@link AddExports} resolves correctly when the target is {@code ALL-UNNAMED}.
     */
    @Test
    void shouldResolveWithAllUnnamedTarget() {
        final var addExports = AddExports.of(
            ModuleName.of("com.example.app"),
            PackageName.of("com.example.app.internal"),
            ModuleName.ALL_UNNAMED);

        assertThat(addExports.resolve(null, null))
            .containsExactly("--add-exports", "com.example.app/com.example.app.internal=ALL-UNNAMED");
    }

    /**
     * Ensure two {@link AddExports} instances with the same values are equal.
     */
    @Test
    void shouldEqualWhenSameValues() {
        final var a = AddExports.of("com.example.app", "com.example.app.internal", "com.example.lib");
        final var b = AddExports.of("com.example.app", "com.example.app.internal", "com.example.lib");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    /**
     * Ensure two {@link AddExports} instances with different values are not equal.
     */
    @Test
    void shouldNotEqualWhenDifferentValues() {
        final var a = AddExports.of("com.example.app", "com.example.app.internal", "com.example.lib");
        final var b = AddExports.of("com.example.app", "com.example.app.other", "com.example.lib");

        assertThat(a).isNotEqualTo(b);
    }
}
