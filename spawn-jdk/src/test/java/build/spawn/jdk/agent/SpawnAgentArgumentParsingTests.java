package build.spawn.jdk.agent;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the argument parsing behaviour of {@link SpawnAgent}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
class SpawnAgentArgumentParsingTests {

    /**
     * Parses the given agent argument string via the private static
     * {@code SpawnAgent.parseArguments} method using reflection.
     *
     * @param arguments the raw agent argument string
     * @return the parsed {@link Properties}
     * @throws Exception if reflection-based invocation fails
     */
    private static Properties parseArguments(final String arguments)
        throws Exception {

        final Method method = SpawnAgent.class.getDeclaredMethod("parseArguments", String.class);
        method.setAccessible(true);
        return (Properties) method.invoke(null, arguments);
    }

    /**
     * Ensure that a value containing an {@code =} character is preserved in full rather than
     * being silently discarded.
     *
     * @throws Exception if reflection-based invocation fails
     */
    @Test
    void shouldPreserveValueContainingEqualsSign()
        throws Exception {

        final var properties = parseArguments("machine=host:1234,token=abc=def");

        assertThat(properties.getProperty("machine")).isEqualTo("host:1234");
        assertThat(properties.getProperty("token")).isEqualTo("abc=def");
    }

    /**
     * Ensure that a value consisting entirely of {@code =} characters (e.g. Base64 padding)
     * is not discarded.
     *
     * @throws Exception if reflection-based invocation fails
     */
    @Test
    void shouldPreserveValueOfOnlyEqualsSignPadding()
        throws Exception {

        final var properties = parseArguments("host=localhost:1234,padding===");

        assertThat(properties.getProperty("host")).isEqualTo("localhost:1234");
        assertThat(properties.getProperty("padding")).isEqualTo("==");
    }
}
