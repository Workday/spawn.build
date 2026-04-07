package build.spawn.docker.option;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PublishPort}.
 *
 * @author brian.oliver
 * @since Mar-2026
 */
class PublishPortTests {

    /**
     * Ensure that configuring multiple external bindings for the same internal port
     * accumulates all bindings rather than silently discarding prior ones.
     */
    @Test
    void shouldAccumulateMultipleBindingsForSamePort() {
        final var objectMapper = new ObjectMapper();
        final var container = objectMapper.createObjectNode();

        // configure two distinct external ports mapped to the same internal port
        PublishPort.of(8080, PublishPort.Type.TCP, 8080).configure(container, objectMapper);
        PublishPort.of(8080, PublishPort.Type.TCP, 9090).configure(container, objectMapper);

        final var portBindings = (ObjectNode) container.path("HostConfig").path("PortBindings");
        final var array = (ArrayNode) portBindings.get("8080/tcp");

        assertThat(array).hasSize(2);
    }
}
