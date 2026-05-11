package build.spawn.jdk.option;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AddReads}.
 *
 * @author reed.vonredwitz
 * @since May-2026
 */
class AddReadsTests {

    /**
     * Ensure {@link AddReads} resolves to the expected command-line tokens.
     */
    @Test
    void shouldResolveToExpectedTokens() {
        final var addReads = AddReads.of("com.example.app", "com.example.lib");

        assertThat(addReads.resolve(null, null))
            .containsExactly("--add-reads", "com.example.app=com.example.lib");
    }

    /**
     * Ensure {@link AddReads} resolves correctly when the target is {@code ALL-UNNAMED}.
     */
    @Test
    void shouldResolveWithAllUnnamedTarget() {
        final var addReads = AddReads.of(ModuleName.of("com.example.app"), ModuleName.ALL_UNNAMED);

        assertThat(addReads.resolve(null, null))
            .containsExactly("--add-reads", "com.example.app=ALL-UNNAMED");
    }

    /**
     * Ensure two {@link AddReads} instances with the same values are equal.
     */
    @Test
    void shouldEqualWhenSameValues() {
        final var a = AddReads.of("com.example.app", "com.example.lib");
        final var b = AddReads.of("com.example.app", "com.example.lib");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    /**
     * Ensure two {@link AddReads} instances with different values are not equal.
     */
    @Test
    void shouldNotEqualWhenDifferentValues() {
        final var a = AddReads.of("com.example.app", "com.example.lib");
        final var b = AddReads.of("com.example.app", "com.example.other");

        assertThat(a).isNotEqualTo(b);
    }
}
