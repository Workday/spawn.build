package build.spawn.platform.local.jdk;

import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the log output of {@link JDKHomeBasedPatternDetector}.
 *
 * @author reed.vonredwitz
 * @since Mar-2026
 */
class JDKHomeBasedPatternDetectorLogTests {

    /**
     * Ensure that when a configured JDK search path does not exist, the debug log message
     * shows both the <em>base</em> directory and the full <em>pattern</em> that was skipped.
     * <p>
     * BUG: The format string in {@link JDKHomeBasedPatternDetector} reads
     * {@code "Skipping path [{0}] for pattern [{0}] as the path does not exist"} — the
     * second placeholder is {@code {0}} (base) when it should be {@code {1}} (pattern).
     * This causes the pattern value to never appear in the log, making the message
     * misleading when diagnosing JDK detection failures.
     * <p>
     * NOTE: This test captures log output via {@code java.util.logging} (JUL) on the assumption
     * that {@code build.base.logging.Logger} delegates to JUL.  If a different backend is used,
     * this test will pass vacuously (no records captured) and must be revisited.
     */
    @Test
    void skipLogMessageShouldShowPatternNotJustBase() {
        final List<LogRecord> capturedRecords = new ArrayList<>();

        final var handler = new Handler() {
            @Override
            public void publish(final LogRecord record) {
                if (record.getMessage() != null && record.getMessage().contains("Skipping path")) {
                    capturedRecords.add(record);
                }
            }

            @Override
            public void flush() {
                // nothing to flush
            }

            @Override
            public void close() throws SecurityException {
                // nothing to close
            }
        };
        handler.setLevel(Level.ALL);

        final var julLogger = java.util.logging.Logger.getLogger(
            JDKHomeBasedPatternDetector.class.getName());
        final var originalLevel = julLogger.getLevel();
        julLogger.addHandler(handler);
        julLogger.setLevel(Level.ALL);

        try {
            // trigger detection — non-existent paths in java.home.properties will produce log records
            new JDKHomeBasedPatternDetector().detect().count();
        } finally {
            julLogger.removeHandler(handler);
            julLogger.setLevel(originalLevel);
        }

        // if any "Skipping path" records were captured, verify each shows the pattern value
        for (final var record : capturedRecords) {
            final var params = record.getParameters();
            if (params != null && params.length >= 2) {
                final var base = params[0].toString();
                final var pattern = params[1].toString();

                // the base and pattern should be distinct values
                assertThat(base).isNotEqualTo(pattern);

                // the formatted message must contain the actual pattern string, not base duplicated
                final var formatted = MessageFormat.format(record.getMessage(), params);
                assertThat(formatted)
                    .as("log message should show pattern '%s' but showed base '%s' twice", pattern, base)
                    .contains(pattern);
            }
        }

        // at least one "Skipping path" record must have been captured for this test to be meaningful
        // (on any platform, some JDK patterns in java.home.properties will not exist)
        assertThat(capturedRecords)
            .as("expected at least one 'Skipping path' log record to be produced during JDK detection")
            .isNotEmpty();
    }
}
