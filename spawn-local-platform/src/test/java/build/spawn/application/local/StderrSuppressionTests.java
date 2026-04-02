package build.spawn.application.local;

import build.base.assertion.Eventually;
import build.base.flow.CompletingSubscriber;
import build.spawn.application.option.Argument;
import build.spawn.application.option.StandardErrorSubscriber;
import build.spawn.platform.local.LocalMachine;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for stderr suppression when no {@link build.spawn.application.Console} is provided.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
class StderrSuppressionTests {

    /**
     * Ensure that stderr from a launched process does not leak to {@link System#err} when no
     * {@link build.spawn.application.Console.Supplier} is configured, even if a
     * {@link StandardErrorSubscriber} is present.
     * <p>
     * When no {@link build.spawn.application.Console.Supplier} is provided,
     * {@link build.spawn.application.AbstractApplication} falls back to a
     * {@link build.spawn.application.console.NullConsole} whose error writer discards all
     * subprocess output.  The subscriber receives the lines; {@link System#err} receives nothing.
     * <p>
     * NOTE: The observed leak was not subprocess stderr (correctly routed via
     * {@link build.spawn.application.console.NullConsole}) but framework launch diagnostics logged
     * at {@code INFO} level by {@link build.spawn.platform.local.LocalLauncher}, which Java Util
     * Logging's default {@code ConsoleHandler} writes to {@link System#err}.  The fix was to log
     * those diagnostics at {@code DEBUG} level, below the default handler threshold.
     */
    @Test
    void stderrShouldNotLeakToSystemErrWhenNoConsoleIsProvided() {
        final var captured = new ByteArrayOutputStream();
        final var original = System.err;

        // redirect System.err to observe whether anything leaks
        System.setErr(new PrintStream(captured));

        try {
            final var subscriber = new CompletingSubscriber<String>();

            // "java -help" writes its usage text to stderr; observe it via the subscriber
            final var usageObserved = subscriber.when(s -> s.startsWith("Usage:"));

            // launch WITHOUT Console.ofSystem() — only a StandardErrorSubscriber is provided
            try (final var app = LocalMachine.get()
                .launch("java",
                    Argument.of("-help"),
                    StandardErrorSubscriber.of(subscriber))) {

                Eventually.assertThat(app.onStart()).isCompleted();

                // the subscriber should still receive the stderr content
                Eventually.assertThat(usageObserved).isCompleted();

                Eventually.assertThat(app.onExit()).isCompleted();
            }
        } finally {
            System.setErr(original);
        }

        // nothing should have leaked to System.err
        assertThat(captured.toString())
            .as("stderr should be suppressed when no Console is configured, but output leaked to System.err")
            .isEmpty();
    }
}
