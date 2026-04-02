package build.spawn.docker.okhttp.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ContainerInformation}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
class ContainerInformationTests {

    /**
     * Ensure that {@link ContainerInformation#name()} returns the plain container name
     * without JSON quote characters.
     * <p>
     * BUG: {@link ContainerInformation#name()} calls {@code jsonNode().get("Name").toString()},
     * which returns the JSON representation of the text node (e.g. {@code "\"/my-container\""})
     * rather than the raw text value.  It should call {@code asText()} instead.
     *
     * @throws Exception if reflection-based field injection fails
     */
    @Test
    void nameShouldReturnUnquotedContainerName() throws Exception {
        final var objectMapper = new ObjectMapper();
        final var jsonNode = objectMapper.createObjectNode();
        jsonNode.put("Name", "/my-container");

        final var info = new ContainerInformation();

        // inject the JsonNode via reflection — AbstractJsonBasedResult is an open module
        final Field field = AbstractJsonBasedResult.class.getDeclaredField("jsonNode");
        field.setAccessible(true);
        field.set(info, jsonNode);

        // name() must return the plain text, not the JSON-serialized form including quotes
        assertThat(info.name()).isEqualTo("/my-container");
    }
}
