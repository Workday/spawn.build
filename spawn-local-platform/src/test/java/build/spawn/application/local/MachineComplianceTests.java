package build.spawn.application.local;

import build.base.assertion.Eventually;
import build.base.flow.CompletingSubscriber;
import build.base.foundation.CompletableFutures;
import build.base.option.WorkingDirectory;
import build.spawn.application.Application;
import build.spawn.application.Console;
import build.spawn.application.Customizer;
import build.spawn.application.Machine;
import build.spawn.application.option.Argument;
import build.spawn.application.option.StandardErrorSubscriber;
import build.spawn.application.option.StandardOutputSubscriber;
import build.spawn.option.EnvironmentVariable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Compliance tests for all {@link Machine}s.
 *
 * @author graeme.campbell
 * @since Jun-2019
 */
public interface MachineComplianceTests {

    /**
     * Obtain the {@link Machine} on which to execute tests.
     *
     * @return the {@link Machine}
     */
    Machine machine();

    /**
     * Ensure a {@link Machine} can launch a JVM.
     */
    @Test
    default void shouldLaunchVirtualMachine() {

        try (Application application = machine()
            .launch("java",
                Argument.of("-help"),
                Console.ofSystem())) {

            Eventually.assertThat(application.onExit())
                .isCompleted();
        }
    }

    /**
     * Ensure a {@link Machine} can launch "echo hello world"
     */
    @Test
    default void shouldOutputHelloWorld() {

        try (Application application = machine()
            .launch("echo",
                Argument.of("Hello World"),
                Console.ofSystem())) {

            Eventually.assertThat(application.onExit())
                .isCompleted();
        }
    }

    /**
     * Ensure a {@link Machine}'s name can be evaluated by a resolvable {@link Argument}.
     */
    @Test
    default void shouldOutputHelloLocalByEvaluatingMachineName() {

        final var machine = machine();
        final var completingSubscriber = new CompletingSubscriber<String>();
        final var nameObserved = completingSubscriber
            .when(s -> s.contains("Hello " + machine.name()));

        try (Application application = machine
            .launch("echo",
                Argument.of("Hello ${machine.name()}"),
                Console.ofSystem(),
                StandardOutputSubscriber.of(completingSubscriber))) {

            Eventually.assertThat(nameObserved)
                .isCompleted();

            Eventually.assertThat(application.onExit())
                .isCompleted();
        }
    }

    /**
     * Ensure a {@link Machine} can launch an Executable which is an expression.
     */
    @Test
    default void shouldEvaluateExecutableEchoExpression() {

        final var completingSubscriber = new CompletingSubscriber<String>();

        final var helloWorldObserved = completingSubscriber.when(s -> s.contains("Hello World"));

        try (Application application = machine()
            .launch("${'e'.concat('cho')}",
                Argument.of("Hello World"),
                Console.ofSystem(),
                StandardOutputSubscriber.of(completingSubscriber))) {

            Eventually.assertThat(helloWorldObserved)
                .isCompleted();

            Eventually.assertThat(application.onExit())
                .isCompleted();
        }
    }

    /**
     * Ensure a {@link Machine} can launch an application using a {@link WorkingDirectory} which is an expression.
     */
    @Test
    default void shouldEvaluateWorkingDirectoryExpression() {

        try (Application application = machine()
            .launch("echo",
                Argument.of("Hello World"),
                Console.ofSystem(),
                WorkingDirectory.of("${'/usr/'.concat('local')}"))) {

            assertThat(application.configuration()
                .getValue(WorkingDirectory.class))
                .isEqualTo("/usr/local");

            Eventually.assertThat(application.onExit())
                .isCompleted();
        }
    }

    /**
     * Ensure a {@link Machine} can set {@link EnvironmentVariable}s using expressions and that they are reflected
     * in the launched {@link Application}'s running environment.
     */
    @Test
    default void shouldEvaluateEnvironmentVariableWithExpression() {

        final var machine = machine();
        final var completingObserver = new CompletingSubscriber<String>();
        final var localObserved = completingObserver
            .when(s -> s.contains(machine.name()));

        try (Application application = machine
            .launch("printenv",
                Argument.of("LC_MACHINE_NAME"),
                Console.ofSystem(),
                EnvironmentVariable.of("LC_MACHINE_NAME", "${machine.name()}"),
                StandardOutputSubscriber.of(completingObserver))) {

            Eventually.assertThat(localObserved)
                .isCompleted();

            Eventually.assertThat(application.onExit())
                .isCompleted();
        }
    }

    /**
     * Ensure a {@link Machine} can launch java and we can observe stderr.
     */
    @Test
    default void shouldObserveLaunchingAVirtualMachine() {

        final var completingSubscriber = new CompletingSubscriber<String>();

        // establish a CompletableFuture that is completed when "Usage:" is observed
        final var usageObserved = completingSubscriber.when(s -> s.startsWith("Usage:"));

        // launch "java" with the "-help" argument
        try (Application java = machine()
            .launch("java",
                Argument.of("-help"),
                Console.ofSystem(),
                StandardErrorSubscriber.of(completingSubscriber))) {

            // ensure the application started
            Eventually.assertThat(java.onStart())
                .isCompleted();

            // ensure we observe "Usage:" instructions from "java"
            Eventually.assertThat(usageObserved)
                .isCompleted();

            // ensure "java" exits
            Eventually.assertThat(java.onExit())
                .isCompleted();

            // ensure "java" exits with the expected value
            assertThat(java.exitValue())
                .isPresent();

            assertThat(java.exitValue().getAsInt())
                .isEqualTo(0);
        }
    }

    /**
     * Ensure {@link Customizer}s methods are invoked when launching an {@link Application}.
     */
    @Test
    default void shouldObserveApplicationCustomizerCallbacks() {

        // establish a Mock Application.Customizer to ensure expected invocations
        final Customizer<?> customizer = Mockito.mock(Customizer.class);
        Mockito.when(customizer.onStart(any(), any(), any()))
            .thenReturn(CompletableFutures.completedFuture());

        try (Application application = machine()
            .launch("echo",
                Argument.of("Hello World"),
                customizer)) {

            Eventually.assertThat(application.onStart())
                .isCompleted();

            Eventually.assertThat(application.onExit())
                .isCompleted();
        }

        Mockito.verify(customizer, times(1))
            .onLaunching(any(), any(), any());

        Mockito.verify(customizer, times(1))
            .onLaunched(any(), any(), any());

        Mockito.verify(customizer, times(1))
            .onStart(any(), any(), any());

        Mockito.verify(customizer, times(1))
            .onShuttingDown(any(), any(), any());

        Mockito.verify(customizer, times(1))
            .onTerminated(any(), any(), any());

        Mockito.verify(customizer, never())
            .onDestroying(any(), any(), any());

        Mockito.verify(customizer, never())
            .onSuspending(any(), any(), any());

        Mockito.verify(customizer, never())
            .onResuming(any(), any(), any());
    }
}
