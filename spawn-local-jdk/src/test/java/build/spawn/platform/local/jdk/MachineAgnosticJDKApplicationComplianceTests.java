package build.spawn.platform.local.jdk;

import build.base.archiving.JarBuilder;
import build.base.assertion.Eventually;
import build.base.configuration.Option;
import build.base.flow.CompletingSubscriber;
import build.base.flow.Producer;
import build.base.flow.RecordingSubscriber;
import build.base.foundation.iterator.matching.IteratorPatternMatchers;
import build.base.foundation.unit.MemorySize;
import build.base.io.SerializableCallable;
import build.base.naming.UniqueNameGenerator;
import build.base.network.Network;
import build.base.network.Server;
import build.base.option.Timeout;
import build.spawn.application.Application;
import build.spawn.application.Console;
import build.spawn.application.Machine;
import build.spawn.application.option.ApplicationSubscriber;
import build.spawn.application.option.Argument;
import build.spawn.application.option.Orphanable;
import build.spawn.application.option.StandardOutputSubscriber;
import build.spawn.jdk.JDKApplication;
import build.spawn.jdk.JDKSpecification;
import build.spawn.jdk.option.JDKOption;
import build.spawn.jdk.option.Jar;
import build.spawn.jdk.option.MainClass;
import build.spawn.jdk.option.MaximumHeapSize;
import build.spawn.jdk.option.SystemProperty;
import build.spawn.platform.local.jdk.application.CommandLineParserApplication;
import build.spawn.platform.local.jdk.application.DiagnosticsApplication;
import build.spawn.platform.local.jdk.application.HangingApplication;
import build.spawn.platform.local.jdk.application.HelloWorld;
import build.spawn.platform.local.jdk.application.ParentApplication;
import build.spawn.platform.local.jdk.application.PrintingApplication;
import build.spawn.platform.local.jdk.application.PublishingApplication;
import build.spawn.platform.local.jdk.application.SleepingApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * {@link Machine} agnostic compliance tests for {@link JDKApplication}s.
 *
 * @author brian.oliver
 * @since Jan-2025
 */
public interface MachineAgnosticJDKApplicationComplianceTests {

    /**
     * The {@link Machine} to launch and manage {@link JDKApplication}s.
     *
     * @return the {@link Machine}
     */
    Machine machine();

    /**
     * The {@link Timeout} in which assertions cannot exceed to be considered correct.
     *
     * @return the {@link Timeout} for an individual assertions
     */
    default Timeout timeout() {
        return Timeout.of(Duration.ofSeconds(5));
    }

    /**
     * Ensure a {@link Machine} can launch the {@link HelloWorld} {@link JDKApplication}.
     */
    @Test
    default void shouldLaunchHelloWorldApplication() {

        // establish a CompletingSubscriber from which we can create CompletableFutures when certain items are observed
        final var subscriber = new CompletingSubscriber<String>();

        // establish a CompletableFuture that is completed when "Hello World" is observed
        final var greeting = "Hello World";
        final var message = subscriber.when(s -> s.startsWith(greeting));

        // launch HelloWorld application
        try (var application = machine().launch(JDKApplication.class,
            MainClass.of(HelloWorld.class),
            Console.ofSystem(),
            StandardOutputSubscriber.of(subscriber))) {

            // ensure the application started
            Eventually.assertThat(application.onStart())
                .isCompleted();

            // ensure we observe "Hello World"
            Eventually.assertThat(message)
                .isCompletedWithValue(greeting);

            // ensure application exits
            Eventually.assertThat(application.onExit())
                .isCompleted();

            // ensure application exits with the expected value
            assertThat(application.exitValue())
                .isPresent();

            assertThat(application.exitValue().getAsInt())
                .isEqualTo(42);
        }
    }

    /**
     * Ensure a {@link Machine} can launch the {@link DiagnosticsApplication} {@link JDKApplication}.
     */
    @Test
    default void shouldLaunchDiagnosticsApplication() {

        try (var application = machine().launch(JDKApplication.class,
            MainClass.of(DiagnosticsApplication.class),
            Console.ofSystem(),
            MaximumHeapSize.of(256, MemorySize.MiB))) {

            // ensure application exits
            Eventually.assertThat(application.onExit())
                .isCompleted();

            assertThat(application.exitValue())
                .isPresent();

            assertThat(application.exitValue().getAsInt())
                .isEqualTo(0);
        }
    }

    /**
     * Ensure {@link SystemProperty}s can be set for a {@link JDKApplication}.
     */
    @Test
    default void shouldLaunchApplicationWithSystemProperties() {

        // establish an Observer from which we can create CompletableFutures when certain items are observed
        final var subscriber = new CompletingSubscriber<String>();

        final var custom1 = subscriber.when(s -> s.contains("custom.1"));
        final var custom2 = subscriber.when(s -> s.contains("custom.2"));
        final var customEmpty = subscriber.when(s -> s.contains("custom.empty"));

        // launch DiagnosticsApplication using SystemProperty(s)
        try (var application = machine().launch(JDKApplication.class,
            MainClass.of(DiagnosticsApplication.class),
            Console.ofSystem(),
            SystemProperty.of("custom.1", "1"),
            SystemProperty.of("custom.2", "2"),
            SystemProperty.of("custom.empty"),
            StandardOutputSubscriber.of(subscriber))) {

            // ensure application exits
            Eventually.assertThat(application.onExit())
                .isCompleted();

            // ensure the output is as expected
            Eventually.assertThat(custom1)
                .isCompleted();

            Eventually.assertThat(custom2)
                .isCompleted();

            Eventually.assertThat(customEmpty)
                .isCompleted();
        }
    }

    /**
     * Ensure a {@link JDKApplication} can be launched with a {@link SystemProperty} that is the result of evaluating
     * an expression using the {@link Machine}'s name as a method call.
     */
    @Test
    default void shouldEvaluateSystemPropertiesWithExpressions() {

        final var machine = machine();

        // establish an Observer from which we can create CompletableFutures when certain items are observed
        final var subscriber = new CompletingSubscriber<String>();
        final var machineName = subscriber.when(s -> s.contains("*" + machine.name() + "*"));

        try (var application = machine.launch(JDKApplication.class,
            MainClass.of(DiagnosticsApplication.class),
            Console.ofSystem(),
            SystemProperty.of("machine.name", "*${machine.name()}*"),
            StandardOutputSubscriber.of(subscriber))) {

            Eventually.assertThat(machineName)
                .isCompleted();

            Eventually.assertThat(application.onExit())
                .isCompleted();
        }
    }

    /**
     * Ensure a {@link Machine} can launch and wait for the {@link SleepingApplication} to terminate.
     */
    @Test
    default void shouldLaunchSleepingApplication() {

        final var start = Instant.now();

        try (var application = machine().launch(JDKApplication.class,
            MainClass.of(SleepingApplication.class),
            Console.ofSystem(),
            Argument.of("PT1S"))) {

            // ensure application exits
            Eventually.assertThat(application.onExit())
                .isCompleted();
        }

        final var end = Instant.now();

        assertThat(Duration.between(start, end))
            .isGreaterThan(Duration.ofSeconds(1));
    }

    /**
     * Ensure a {@link Machine} can launch and wait for the {@link SleepingApplication} to terminate.
     */
    @Test
    default void shouldLaunchSleepingApplicationWithJDKOption() {

        final var start = Instant.now();

        try (var application = machine().launch(JDKApplication.class,
            MainClass.of(SleepingApplication.class),
            Console.ofSystem(),
            Argument.of("PT1S"),
            JDKOption.of("-Xms256m"),
            JDKOption.of("-Xmx512m"))) {

            // ensure the custom option is defined
            assertThat(application.configuration()
                .stream(JDKOption.Custom.class))
                .hasSize(2);

            // ensure application exits
            Eventually.assertThat(application.onExit())
                .isCompleted();
        }

        final var end = Instant.now();

        assertThat(Duration.between(start, end))
            .isGreaterThan(Duration.ofSeconds(1));
    }

    /**
     * Ensure a {@link Machine} can launch and shutdown the {@link SleepingApplication}.
     */
    @Test
    default void shouldShutdownSleepingApplication() {

        final var start = Instant.now();

        try (var application = machine().launch(JDKApplication.class,
            MainClass.of(SleepingApplication.class),
            Console.ofSystem(),
            Argument.of("PT3S"))) {

            application.shutdown();

            // ensure application exits
            Eventually.assertThat(application.onExit())
                .isCompleted();
        }

        final var end = Instant.now();

        assertThat(Duration.between(start, end))
            .isLessThan(timeout().get());
    }

    /**
     * Ensure a {@link Machine} can launch and destroy the {@link SleepingApplication}.
     */
    @Test
    default void shouldDestroySleepingApplication() {
        final var start = Instant.now();

        try (var application = machine().launch(JDKApplication.class,
            MainClass.of(SleepingApplication.class),
            Console.ofSystem(),
            Argument.of("PT3S"))) {

            application.destroy();

            // ensure application exits
            Eventually.assertThat(application.onExit())
                .isCompleted();
        }

        final var end = Instant.now();

        assertThat(Duration.between(start, end))
            .isLessThan(timeout().get());
    }

    /**
     * Ensure a {@link Machine} can launch and shutdown the {@link HangingApplication}.
     */
    @Test
    default void shouldShutdownHangingApplication() {
        final var start = Instant.now();

        try (var application = machine().launch(JDKApplication.class,
            MainClass.of(HangingApplication.class),
            Console.ofSystem())) {

            application.shutdown();

            // ensure application exits
            Eventually.assertThat(application.onExit())
                .isCompleted();
        }

        final var end = Instant.now();

        assertThat(Duration.between(start, end))
            .isLessThan(timeout().get());
    }

    /**
     * Ensure a {@link Machine} can launch and destroy the {@link HangingApplication}.
     */
    @Test
    default void shouldDestroyHangingApplication() {
        final var start = Instant.now();

        try (var application = machine().launch(JDKApplication.class,
            MainClass.of(HangingApplication.class),
            Console.ofSystem())) {

            application.destroy();

            // ensure application exits
            Eventually.assertThat(application.onExit())
                .isCompleted();
        }

        final var end = Instant.now();

        assertThat(Duration.between(start, end))
            .isLessThan(timeout().get());
    }

    /**
     * Ensure a lambda-based {@link SerializableCallable} can be executed in a {@link JDKApplication}, returning
     * a customized {@link SystemProperty}.
     */
    @Test
    default void shouldExecuteLambdaInAnApplication() {

        // the key and value for a custom system property
        final var key = "MESSAGE";
        final var value = "HELLO";

        try (var application = machine().launch(JDKApplication.class,
            MainClass.of(SleepingApplication.class),
            Console.ofSystem(),
            Argument.of("PT5S"),
            SystemProperty.of(key, value))) {

            // submit a lambda expression to obtain a specific system property
            final var result = application.submit(() -> System.getProperty(key));

            // ensure the property is returned
            Eventually.assertThat(result)
                .isCompletedWithValue(value);
        }
    }

    /**
     * Ensure a lambda-based {@link SerializableCallable} that throws an {@link Exception} can be executed in a
     * {@link JDKApplication}.
     */
    @Test
    default void shouldExecuteThrowingLambdaInAnApplication() {

        try (var application = machine().launch(JDKApplication.class,
            MainClass.of(SleepingApplication.class),
            Console.ofSystem(),
            Argument.of("PT5S"))) {

            // submit a lambda expression that throws an exception
            final CompletableFuture<String> result = application.submit(() -> {
                throw new UnsupportedOperationException("Oh no");
            });

            // ensure the property isn't returned
            Eventually.assertThat(result)
                .isCompletedExceptionally()
                .hasCauseInstanceOf(UnsupportedOperationException.class);
        }
    }

    /**
     * Ensure a fail-fast attempting to submit a {@link SerializableCallable} when an {@link JDKApplication} has
     * terminated.
     */
    @Test
    default void shouldFailToExecuteLambdaInAnApplication() {

        try (var application = machine().launch(JDKApplication.class,
            MainClass.of(SleepingApplication.class),
            Console.ofSystem(),
            Argument.of("PT1S"))) {

            // terminate the application immediately (don't even wait for it to start)
            application.close();

            // submit a lambda expression
            final CompletableFuture<String> javaHome = application.submit(() -> System.getProperty("java.home"));

            // ensure the submission fails
            Eventually.assertThat(javaHome)
                .isCompletedExceptionally();
        }
    }

    /**
     * Ensure a child application is terminated when the parent is terminated (the default {@link Orphanable} behavior).
     */
    @Test
    default void shouldTerminateChildApplicationWhenParentTerminates()
        throws Exception {

        final var machine = machine();

        // establish a Server to which the ClientApplication will connect
        // (so we can observe client termination)
        try (var server = new Server(42).start()) {

            // find the first address of the machine that's of the same class as the Server
            // (ensures that if the Server is using IPv4, the address returned is IPv4)
            final var inetAddress = machine.addresses()
                .filter(Network.isOfClass(server.getLocalAddress()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Failed to establish a local InetAddress for the Server"));

            // create a URI for the ClientApplication
            final var uri = new URI("spawn", null, inetAddress.getHostAddress(), server.getLocalPort(),
                null, null, null);

            // launch the ParentApplication (which in turn launches the ChildApplication)
            try (var parentApplication = machine.launch(JDKApplication.class,
                MainClass.of(ParentApplication.class),
                Console.ofSystem(),
                SystemProperty.of("uri", uri.toString()))) {

                // ensure ParentApplication has started
                Eventually.assertThat(parentApplication.onStart())
                    .isCompleted();

                // ensure the ChildApplication has connected to the Server
                final var childConnection = server.onConnection(42);
                Eventually.assertThat(childConnection)
                    .isCompleted();

                // terminate the ParentApplication
                parentApplication.shutdown();

                // ensure the ParentApplication terminates
                Eventually.assertThat(parentApplication.onExit())
                    .isCompleted();

                // ensure the ChildApplication connection is closed
                Eventually.assertThat(childConnection.get().onClosed())
                    .isCompleted();
            }
        }
    }

    /**
     * Ensure a child application is not terminated when the parent is terminated (using an {@link Orphanable}
     * behavior).
     *
     * @throws Exception when test infrastructure fails
     */
    @Test
    default void shouldNotTerminateChildApplicationWhenParentTerminates()
        throws Exception {

        final var machine = machine();

        // establish a Server to which the ClientApplication will connect
        // (so we can observe client termination)
        try (var server = new Server(42).start()) {

            // find the first address of the machine that's of the same class as the Server
            // (ensures that if the Server is using IPv4, the address returned is IPv4)
            final var inetAddress = machine.addresses()
                .filter(Network.isOfClass(server.getLocalAddress()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Failed to establish a local InetAddress for the Server"));

            // create a URI for the ClientApplication
            final var uri = new URI("spawn", null, inetAddress.getHostAddress(), server.getLocalPort(),
                null, null, null);

            // launch the ParentApplication (which in turn launches the ChildApplication)
            try (var parentApplication = machine.launch(JDKApplication.class,
                MainClass.of(ParentApplication.class),
                Console.ofSystem(),
                SystemProperty.of("uri", uri.toString()),
                SystemProperty.of("orphanable", Orphanable.ENABLED.name()))) {

                // ensure ParentApplication has started
                Eventually.assertThat(parentApplication.onStart())
                    .isCompleted();

                // ensure the ChildApplication has connected to the Server
                final var childConnection = server.onConnection(42);
                Eventually.assertThat(childConnection)
                    .isCompleted();

                // terminate the ParentApplication
                parentApplication.shutdown();

                // ensure the ParentApplication terminates
                Eventually.assertThat(parentApplication.onExit())
                    .isCompleted();

                // ensure the ChildApplication connection is still operational
                Eventually.assertThat(childConnection.get().submit(() -> "PINGPONG"))
                    .isCompletedWithValue("PINGPONG");
            }
        }
    }

    /**
     * Ensure a {@link JDKApplication} may be launched from a jar.
     *
     * @throws IOException should it not be possible to create the jar
     */
    @Test
    default void shouldLaunchHelloWorldApplicationFromJar()
        throws IOException {

        // create a temporary Jar containing the HelloWorld application
        // (setting it as the main-class)
        final var archiveBuilder = new JarBuilder()
            .withManifestVersion("1.0.0")
            .withMainClass(HelloWorld.class);

        archiveBuilder.content().add(HelloWorld.class);

        final Path jarFile = Files.createTempFile("helloworld-", ".jar");
        jarFile.toFile().deleteOnExit();

        archiveBuilder.build(jarFile);

        // attempt to launch the jar

        // establish an Observer from which we can create CompletableFutures when certain items are observed
        final var subscriber = new CompletingSubscriber<String>();

        // establish a CompletableFuture that is completed when "Hello World" is observed
        final var greeting = "Hello World";
        final var message = subscriber.when(s -> s.startsWith(greeting));

        // launch HelloWorld application
        try (var application = machine().launch(JDKApplication.class,
            Jar.of(jarFile),
            Console.ofSystem(),
            StandardOutputSubscriber.of(subscriber))) {

            // ensure the application started
            Eventually.assertThat(application.onStart())
                .isCompleted();

            // ensure we observe "Hello World"
            Eventually.assertThat(message)
                .isCompletedWithValue(greeting);

            // ensure application exits
            Eventually.assertThat(application.onExit())
                .isCompleted();

            // ensure application exits with the expected value
            assertThat(application.exitValue().isPresent())
                .isTrue();

            assertThat(application.exitValue().getAsInt())
                .isEqualTo(42);
        }
    }

    /**
     * Ensure that {@link Producer} can produce items from within a {@link JDKApplication}.
     */
    @Test
    default void shouldPublishAndSubscribe() {

        final var publisherName = "publisher";
        final var recorder = new RecordingSubscriber<PublishingApplication.Item>();
        final var subscriber = ApplicationSubscriber.of(
            publisherName,
            PublishingApplication.Item.class,
            recorder);

        // launch the PublishingApplication with the PublishingObserver
        try (var application = machine().launch(JDKApplication.class,
            MainClass.of(PublishingApplication.class),
            Console.ofSystem(),
            subscriber,
            Argument.of(publisherName))) {

            // ensure the application started
            Eventually.assertThat(application.onStart())
                .isCompleted();

            // ensure application exits
            Eventually.assertThat(application.onExit())
                .isCompleted();

            // the predicate to eventually match
            final var matcher = IteratorPatternMatchers.<PublishingApplication.Item>starts()
                .then().matches(item -> item.message().equals("episode1"))
                .then().matches(item -> item.message().equals("episode2"))
                .then().matches(item -> item.message().equals("episode3"))
                .then().ends();

            assertThat(matcher.test(recorder.filter(item -> item.name().equals("GOT"))))
                .isTrue();
        }
    }

    /**
     * Ensures a {@link PrintingApplication} can be {@link Application#suspend()}ed.
     */
    @Test
    default void shouldSuspendAndResumePrintingApplication() {

        final var uniqueNameGenerator = new UniqueNameGenerator(".");
        final var name = "name";

        try (var application = machine().launch(JDKApplication.class,
            MainClass.of(PrintingApplication.class),
            SystemProperty.of(name, uniqueNameGenerator.next().toLowerCase()),
            Console.ofSystem())) {

            // determine the response time submitting requests to the application (a number of times)
            final var timeout = Timeout.autodetect().get();
            final var start = Instant.now();
            for (int i = 0; i < 100; i++) {
                try {
                    final var submission = application.submit(() -> System.getProperty(name));
                    submission.get(timeout.getSeconds(), TimeUnit.SECONDS);
                }
                catch (final Exception e) {
                    fail("Failed to submit a request to the application", e);
                }
            }
            final var finish = Instant.now();
            final var totalResponseTime = Duration.between(start, finish).toMillis();

            // suspend the application
            Eventually.assertThat(application.suspend())
                .isCompleted();

            // submit a request to the suspended application (it should not be executed)
            final var submission = application.submit(() -> System.getProperty(name));

            // ensure the execution does not complete within the total response time
            try {
                submission.get(totalResponseTime, TimeUnit.MILLISECONDS);
                fail("The submitted requests to the suspended application should not have succeeded!");
            }
            catch (final TimeoutException e) {
                // the timeout was expected, so let's resume
                Eventually.assertThat(application.resume())
                    .isCompleted();
            }
            catch (final Exception e) {
                fail("The submitted request to the suspended application failed", e);
            }

            // ensure the submitted executable is completed
            Eventually.assertThat(submission)
                .isCompleted();
        }
    }

    /**
     * Ensures that {@link Application#suspend()}ing a {@link SleepingApplication} that is not alive returns a non-zero
     * exit code.
     */
    @Test
    default void shouldNotSuspendDeadApplication() {

        try (var application = machine().launch(JDKApplication.class,
            MainClass.of(SleepingApplication.class),
            Console.ofSystem())) {

            Eventually.assertThat(application.onExit())
                .isCompleted();

            Eventually.assertThat(application.suspend())
                .isCompleted();
        }
    }

    /**
     * Ensures that {@link Application#resume()}ing a {@link SleepingApplication} that is not alive returns a non-zero
     * exit code.
     */
    @Test
    default void shouldResumeDeadApplication() {

        try (var application = machine().launch(JDKApplication.class,
            MainClass.of(SleepingApplication.class),
            Console.ofSystem())) {

            Eventually.assertThat(application.onExit())
                .isCompleted();

            Eventually.assertThat(application.resume())
                .isCompleted();
        }
    }

    /**
     * Ensures that {@link Application#resume()}ing a {@link SleepingApplication} that is not
     * {@link Application#suspend()}ed does not throw an exception.
     */
    @Test
    default void shouldResumeRunningApplication() {

        try (var application = machine().launch(JDKApplication.class,
            MainClass.of(HangingApplication.class),
            Console.ofSystem())) {

            Eventually.assertThat(application.resume())
                .isCompleted();
        }
    }

    /**
     * Ensures a {@link HangingApplication} that is {@link Application#suspend()}ed can be destroyed.
     */
    @Test
    default void shouldDestroySuspendedApplication() {

        try (var application = machine().launch(JDKApplication.class,
            MainClass.of(HangingApplication.class),
            Console.ofSystem(),
            Argument.of("PT3S"))) {

            Eventually.assertThat(application.suspend())
                .isCompleted();

            application.destroy();

            // ensure application exits
            Eventually.assertThat(application.onExit())
                .isCompleted();
        }
    }

    /**
     * Provides parameters to {@link #shouldLaunchCommandLineParserApplication(String, int, Option[])}.
     *
     * @return a {@link Stream} of {@link Arguments}
     */
    static Stream<Arguments> shouldLaunchCommandLineParserApplication() {
        return Stream.of(
            Arguments.of("Hello World", 0, null),
            Arguments.of("Arguments!", 5, new Option[] {
                Argument.of("--exit-value"),
                Argument.of("5"),
                Argument.of("--text"),
                Argument.of("Arguments!") }),
            Arguments.of("Options...BOOM", 23, new Option[] {
                CommandLineParserApplication.ExitValueOption.of(23),
                CommandLineParserApplication.TextOption.of("Options...BOOM") }));
    }

    /**
     * Ensures a {@link Machine} can launch the {@link CommandLineParserApplication} {@link JDKApplication} and
     * parse options that are converted to arguments and back to options.
     *
     * @param expectedText      the expected text to be observed
     * @param expectedExitValue the expected exit value
     * @param additionalOptions any additional options to pass to the application
     */
    @MethodSource
    @ParameterizedTest
    default void shouldLaunchCommandLineParserApplication(final String expectedText,
                                                          final int expectedExitValue,
                                                          final Option[] additionalOptions) {

        // establish an Observer from which we can create CompletableFutures when certain items are observed
        final var subscriber = new CompletingSubscriber<String>();

        // establish a CompletableFuture that is completed when "Hello World" is observed
        final var futureHelloWorld = subscriber.when(s -> s.startsWith(expectedText));

        // establish an observer of stdout output
        final var specification = new JDKSpecification()
            .withMainClass(CommandLineParserApplication.class)
            .withConsole(Console.ofSystem())
            .with(StandardOutputSubscriber.of(subscriber))
            .with(additionalOptions);

        // launch HelloWorld Java application with no options for "Hello World" and -1
        try (var application = machine().launch(specification)) {

            // ensure the application started
            Eventually.assertThat(application.onStart())
                .isCompleted();

            // ensure we observe "Hello World"
            Eventually.assertThat(futureHelloWorld)
                .isCompletedWithValue(expectedText);

            // ensure application exits
            Eventually.assertThat(application.onExit())
                .isCompleted();

            // ensure application exits with the expected value
            assertThat(application.exitValue().isPresent())
                .isTrue();

            assertThat(application.exitValue().getAsInt())
                .isEqualTo(expectedExitValue);
        }
    }
}
