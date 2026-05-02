package build.spawn.docker.jdk.model;

import build.base.json.Json;
import build.base.json.JsonObject;
import build.base.json.JsonValue;
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
     *
     * @throws Exception if reflection-based field injection fails
     */
    @Test
    void nameShouldReturnUnquotedContainerName() throws Exception {
        final JsonValue jsonValue = JsonObject.builder()
            .put("Name", "/my-container")
            .build();

        final var info = new ContainerInformation();

        final Field field = AbstractJsonBasedResult.class.getDeclaredField("jsonValue");
        field.setAccessible(true);
        field.set(info, jsonValue);

        assertThat(info.name()).isEqualTo("/my-container");
    }

    @Test
    void ipAddressShouldReturnEmptyStringWhenJsonNull() throws Exception {
        final JsonValue jsonValue = Json.parse("{\"NetworkSettings\":{\"IPAddress\":null}}");

        final var info = new ContainerInformation();

        final Field field = AbstractJsonBasedResult.class.getDeclaredField("jsonValue");
        field.setAccessible(true);
        field.set(info, jsonValue);

        assertThat(info.ipAddress()).isEmpty();
    }

    @Test
    void publishedPortsShouldReturnEmptyStreamWhenPortsIsJsonNull() throws Exception {
        final JsonValue jsonValue = Json.parse("{\"NetworkSettings\":{\"Ports\":null}}");

        final var info = new ContainerInformation();

        final Field field = AbstractJsonBasedResult.class.getDeclaredField("jsonValue");
        field.setAccessible(true);
        field.set(info, jsonValue);

        assertThat(info.publishedPorts()).isEmpty();
    }

    @Test
    void linksShouldReturnEmptyStreamWhenLinksIsJsonNull() throws Exception {
        final JsonValue jsonValue = Json.parse("{\"HostConfig\":{\"Links\":null}}");

        final var info = new ContainerInformation();

        final Field field = AbstractJsonBasedResult.class.getDeclaredField("jsonValue");
        field.setAccessible(true);
        field.set(info, jsonValue);

        assertThat(info.links()).isEmpty();
    }

    @Test
    void pidShouldReturnNegativeOneWhenAbsent() throws Exception {
        final JsonValue jsonValue = JsonObject.builder().build();

        final var info = new ContainerInformation();

        final Field field = AbstractJsonBasedResult.class.getDeclaredField("jsonValue");
        field.setAccessible(true);
        field.set(info, jsonValue);

        assertThat(info.pid()).isEqualTo(-1L);
    }
}
