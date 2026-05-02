package build.spawn.docker.option;

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
     * Ensure that two {@link PublishPort} instances with the same internal port but different external ports
     * are not equal, preserving the distinct bindings when collected.
     */
    @Test
    void distinctExternalPortsProduceDifferentInstances() {
        final var first = PublishPort.of(8080, PublishPort.Type.TCP, 8080);
        final var second = PublishPort.of(8080, PublishPort.Type.TCP, 9090);

        assertThat(first).isNotEqualTo(second);
        assertThat(first.port()).isEqualTo(8080);
        assertThat(second.port()).isEqualTo(8080);
        assertThat(first.type()).isEqualTo(PublishPort.Type.TCP);
    }
}
