package build.spawn.application.local;

import build.spawn.platform.local.LocalMachine;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Tests for the {@link LocalMachine#close()} behaviour.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
class LocalMachineCloseTests {

    /**
     * Ensure that closing a {@link LocalMachine} does not throw.
     * <p>
     * The platform holds an internal {@link build.base.network.Server} that must be closed; a
     * no-op {@code close()} would leave it open, leaking the bound port.
     */
    @Test
    void closeShouldNotThrow() {
        assertThatCode(() -> new LocalMachine().close()).doesNotThrowAnyException();
    }

    /**
     * Ensure that closing a {@link LocalMachine} multiple times does not throw.
     */
    @Test
    void closeIsIdempotent() {
        final var machine = new LocalMachine();

        assertThatCode(() -> {
            machine.close();
            machine.close();
        }).doesNotThrowAnyException();
    }
}
