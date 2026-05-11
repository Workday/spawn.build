package build.spawn.jdk.option;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AddOpens}.
 *
 * @author reed.vonredwitz
 * @since May-2026
 */
class AddOpensTests {

    /**
     * Ensure {@link AddOpens} resolves to the expected command-line tokens.
     */
    @Test
    void shouldResolveToExpectedTokens() {
        final var addOpens = AddOpens.of("com.example.app", "com.example.app.internal", "com.example.lib");

        assertThat(addOpens.resolve(null, null))
            .containsExactly("--add-opens", "com.example.app/com.example.app.internal=com.example.lib");
    }

    /**
     * Ensure {@link AddOpens} resolves correctly when the target is {@code ALL-UNNAMED}.
     */
    @Test
    void shouldResolveWithAllUnnamedTarget() {
        final var addOpens = AddOpens.of(
            ModuleName.of("com.example.app"),
            PackageName.of("com.example.app.internal"),
            ModuleName.ALL_UNNAMED);

        assertThat(addOpens.resolve(null, null))
            .containsExactly("--add-opens", "com.example.app/com.example.app.internal=ALL-UNNAMED");
    }

    /**
     * Ensure two {@link AddOpens} instances with the same values are equal.
     */
    @Test
    void shouldEqualWhenSameValues() {
        final var a = AddOpens.of("com.example.app", "com.example.app.internal", "com.example.lib");
        final var b = AddOpens.of("com.example.app", "com.example.app.internal", "com.example.lib");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    /**
     * Ensure two {@link AddOpens} instances with different values are not equal.
     */
    @Test
    void shouldNotEqualWhenDifferentValues() {
        final var a = AddOpens.of("com.example.app", "com.example.app.internal", "com.example.lib");
        final var b = AddOpens.of("com.example.app", "com.example.app.other", "com.example.lib");

        assertThat(a).isNotEqualTo(b);
    }
}
