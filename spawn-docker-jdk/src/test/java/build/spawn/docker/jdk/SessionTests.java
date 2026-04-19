package build.spawn.docker.jdk;

import build.base.assertion.Eventually;
import build.base.configuration.Configuration;
import build.base.configuration.ConfigurationBuilder;
import build.base.configuration.Option;
import build.base.naming.UniqueNameGenerator;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.injection.InjectionFramework;
import build.codemodel.jdk.JDKCodeModel;
import build.spawn.docker.Container;
import build.spawn.docker.DockerContextBuilder;
import build.spawn.docker.DockerFileBuilder;
import build.spawn.docker.Execution;
import build.spawn.docker.Image;
import build.spawn.docker.Session;
import build.spawn.docker.Sessions;
import build.spawn.docker.option.Command;
import build.spawn.docker.option.ContainerName;
import build.spawn.docker.option.ExposedPort;
import build.spawn.docker.option.ExtraHost;
import build.spawn.docker.option.ImageName;
import build.spawn.docker.option.Link;
import build.spawn.docker.option.NetworkName;
import build.spawn.docker.option.PublishAllPorts;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link Session}s.
 *
 * @author brian.oliver
 * @since Jun-2021
 */
@EnabledIf("isDockerAvailable")
class SessionTests {

    /**
     * The {@code alpine:latest} image name.
     */
    protected static final String ALPINE_IMAGE = "alpine:latest";

    /**
     * The {@code rabbitmq:latest} image name.
     */
    protected static final String RABBITMQ_IMAGE = "rabbitmq:latest";

    /**
     * The {@code nginx:latest} image name.
     */
    protected static final String NGINX_IMAGE = "nginx:latest";

    /**
     * Creates a new {@link JDKCodeModel} for testing Java-Reflection.
     *
     * @return a new {@link JDKCodeModel}
     */
    public static JDKCodeModel createTypeSystem() {
        final var nameProvider = new NonCachingNameProvider();
        return new JDKCodeModel(nameProvider);
    }

    /**
     * Create a new {@link InjectionFramework}.
     *
     * @return a new {@link InjectionFramework}
     */
    public static InjectionFramework createInjectionFramework() {
        final var typeSystem = createTypeSystem();
        return new InjectionFramework(typeSystem);
    }

    /**
     * Determines if {@code Docker Engine} is available for testing.
     *
     * @return {@code true} if {@code Docker Engine} is available, {@code false} otherwise
     */
    public static boolean isDockerAvailable() {
        return Sessions.factories(createInjectionFramework())
            .anyMatch(Session.Factory::isOperational);
    }

    /**
     * Creates a new {@link Session} to use for testing.
     *
     * @return a new {@link Session}
     */
    public static Session createSession(final Configuration configuration) {
        return Sessions.createSession(createInjectionFramework(), configuration)
            .orElseThrow(() -> new AssertionError("Docker Session could not be created"));
    }

    /**
     * Creates a new {@link Session} to use for testing.
     *
     * @return a new {@link Session}
     */
    public static Session createSession(final Option... options) {
        final var builder = ConfigurationBuilder.create()
            .include(options);

        // TODO: include the docker authentication information from environment variables
        // eg: DOCKER_REGISTRY (https://docker-public-artifactory.workday.com)
        // and either DOCKER_USERNAME + DOCKER_PASSWORD
        // or DOCKER_IDENTITY_TOKEN

        return createSession(builder.build());
    }

    @BeforeAll
    public static void beforeAll() {
        // pull the required images once and only once
        try (var session = createSession()) {
            System.out.println("Setup: Commenced: Obtaining the required Docker Images");

            session.images()
                .get(ALPINE_IMAGE)
                .or(() -> session.images()
                    .pull(ALPINE_IMAGE));

            session.images()
                .get(RABBITMQ_IMAGE)
                .or(() -> session.images()
                    .pull(RABBITMQ_IMAGE));

            session.images()
                .get(NGINX_IMAGE)
                .or(() -> session.images()
                    .pull(NGINX_IMAGE));

            System.out.println("Setup: Completed: Obtaining the required Docker Images");
        }
    }

    /**
     * Ensure a {@code Docker} {@link Session} can be created and we can retrieve {@link Session.Information}.
     */
    @Test
    void shouldCreateSessionAndRetrieveSystemInformation() {

        try (var session = createSession()) {
            assertThat(session.inspect())
                .isNotNull();
        }
    }

    /**
     * Ensure a {@code Docker} {@link Session} get an {@link Image}.
     */
    @Test
    void shouldGetAnImage() {
        final var imageName = ALPINE_IMAGE;

        try (var session = createSession()) {
            final var image = session.images()
                .get(imageName)
                .orElseThrow(() -> new AssertionError("Failed to get the image"));

            assertThat(image)
                .isNotNull();
        }
    }

    /**
     * Ensure a {@code Docker} {@link Session} can inspect an existing {@link Image}.
     */
    @Test
    void shouldGetAndInspectAnExistingImage() {

        final var imageName = ALPINE_IMAGE;

        try (var session = createSession()) {
            final var image = session.images()
                .get(imageName)
                .orElseThrow(() -> new AssertionError("Failed to get the image"));

            assertThat(image.id())
                .isNotNull();

            final var information = image
                .inspect()
                .orElseThrow(() -> new AssertionError("Failed to inspect the alpine:latest image"));

            assertThat(image.id())
                .isEqualTo(information.imageId());
        }
    }

    /**
     * Ensure a {@code Docker} {@link Session} can inspect an existing {@link Image}.
     */
    @Test
    void shouldNotPullANonExistingImage() {

        try (Session session = createSession()) {
            final var optional = session.images()
                .get("mrsquiggle:1.0");

            assertThat(optional.isPresent())
                .isFalse();
        }
    }

    /**
     * Ensure a {@code Docker} {@link Session} pull the "hello-world" {@link Image}, then create and
     * start a {@link Container} from it.
     */
    @Test
    void shouldPullAnImageThenCreateAndStartAContainer() {

        try (var session = createSession()) {
            final var image = session.images()
                .get(NGINX_IMAGE)
                .orElseThrow(() -> new AssertionError("Failed to get the required image"));

            try (var container = image.start()) {

                Eventually.assertThat(container.onStart())
                    .isCompleted();

                assertThat(container.image())
                    .isNotNull();

                assertThat(container.id())
                    .isNotNull();

                final var information = container
                    .inspect()
                    .orElseThrow(() -> new AssertionError("Failed to obtain the Container.Information"));

                assertThat(container.id())
                    .isEqualTo(information.containerId());

                container.kill();

                Eventually.assertThat(container.onExit())
                    .isCompleted();

                assertThat(container.exitValue())
                    .isPresent();

                assertThat(container.exitValue().getAsInt())
                    .isEqualTo(137);

                container.remove();
            }
        }
    }

    /**
     * Ensure a {@code Docker} {@link Session} can start and attach to a {@link Container} using
     * the {@link #NGINX_IMAGE} {@link Image}.
     */
    @Test
    void shouldStartAndAttachToAContainer() {

        try (var session = createSession()) {
            final var image = session.images()
                .get(NGINX_IMAGE)
                .orElseThrow(() -> new AssertionError("Failed to get the required image"));

            try (var container = image.start()) {
                Eventually.assertThat(container.onStart())
                    .isCompleted();

                try (var terminal = container.attach()) {
                    assertThat(terminal)
                        .isNotNull();
                }

                container.remove();
            }
        }
    }

    /**
     * Ensure a {@code Docker} {@link Session} can customize, start and attach to a {@link Container} using
     * the {@link #ALPINE_IMAGE} {@link Image}.
     */
    @Test
    void shouldStartAndAttachToACustomizedContainer() {

        try (var session = createSession()) {
            final var image = session.images()
                .get(ALPINE_IMAGE)
                .orElseThrow(() -> new AssertionError("Failed to get the required image"));

            try (var container = image.start(Command.of("echo", "Gudday Mate!"))) {

                try (var terminal = container.attach()) {
                    assertThat(terminal)
                        .isNotNull();
                }

                container.remove();
            }
        }
    }

    /**
     * Ensure a {@code Docker} {@link Session} can customize, start, attach and execute a command inside
     * a {@link Container} using the {@link #ALPINE_IMAGE} {@link Image}.
     */
    @Test
    void shouldLaunchExecutableInACustomizedContainer() {

        try (var session = createSession()) {
            final var image = session.images()
                .get(ALPINE_IMAGE)
                .orElseThrow(() -> new AssertionError("Failed to get the required image"));

            try (var container = image.start(Command.of("sleep", "5"))) {

                Eventually.assertThat(container.onStart())
                    .isCompleted();

                final var message = "Gudday Mate!";
                final var execution = container
                    .createExecutable(Command.of("echo", message))
                    .withTerminal(true)
                    .execute();

                // read the first line from the Terminal of our "echo" Execution
                final var terminal = execution.terminal()
                    .orElseThrow();

                try (var reader = new BufferedReader(terminal.getOutputReader())) {
                    assertThat(reader.readLine())
                        .isEqualTo(message);
                } catch (final IOException e) {
                    throw new AssertionError("Could not read from the Terminal", e);
                }

                assertThat(execution.inspect()
                    .map(Execution.Information::pid))
                    .isPresent();

                Eventually.assertThat(execution.onExit())
                    .isCompleted();

                assertThat(execution.exitValue().getAsInt())
                    .isEqualTo(0);

                container.remove();
            }
        }
    }

    /**
     * Ensure a {@code Docker} {@link Session} can expose a port for a {@link Container}.
     */
    @Test
    void shouldExposePortForAContainer() {

        try (var session = createSession()) {
            final var image = session.images()
                .get(ALPINE_IMAGE)
                .orElseThrow(() -> new AssertionError("Failed to get the required image"));

            try (var container = image.start(
                Command.of("echo", "Gudday Mate!"),
                ExposedPort.of(8080),
                ExtraHost.of("localhost:127.0.0.1"),
                PublishAllPorts.ENABLED)) {

                Eventually.assertThat(container.onStart())
                    .isCompleted();

                assertThat(container.inspect()
                    .flatMap(information -> information.exposedPorts().findFirst()))
                    .contains(ExposedPort.of(8080));

                try (var terminal = container.attach()) {
                    assertThat(terminal)
                        .isNotNull();
                }

                container.remove();
            }
        }
    }

    /**
     * Ensure a {@code Docker} {@link Session} can start, kill and delete a {@link Container} using
     * the {@link #RABBITMQ_IMAGE} {@link Image}.
     */
    @Test
    void shouldStartKillAndRemoveRabbitMQ() {

        try (var session = createSession()) {
            final var image = session.images()
                .get(RABBITMQ_IMAGE)
                .orElseThrow(() -> new AssertionError("Failed to get the required image"));

            try (var container = image.start()) {

                Eventually.assertThat(container.onStart())
                    .isCompleted();

                final var containerInfo = container
                    .inspect()
                    .orElseThrow(() -> new AssertionError("Failed to obtain Container Information"));

                assertThat(containerInfo.exposedPorts().count())
                    .isGreaterThan(0);

                assertThat(containerInfo.pid())
                    .isNotEqualTo(-1);

                assertThat(containerInfo.state())
                    .contains(Container.State.RUNNING);

                final var imageInfo = container.image()
                    .inspect()
                    .orElseThrow(() -> new AssertionError("Failed to determine Image Information"));

                assertThat(imageInfo.exposedPorts().count())
                    .isEqualTo(containerInfo.publishedPorts().count());

                container.kill();

                Eventually.assertThat(container.onExit())
                    .isCompleted();

                assertThat(container
                    .exitValue()
                    .orElseThrow(() -> new AssertionError("Expected an Exit Value to be provided")))
                    .isEqualTo(137);

                assertThat(container.inspect()
                    .orElseThrow(() -> new AssertionError("Failed to determine the Container Information"))
                    .state())
                    .contains(Container.State.EXITED);

                container.remove();
            }
        }
    }

    /**
     * Ensure a {@code Docker} {@link Session} can start, pause and unpause a {@link Container} using
     * the {@code rabbitmq:latest} {@link Image}.
     */
    @Test
    void shouldStartPauseAndUnpauseRabbitMQ() {

        try (var session = createSession()) {
            final var image = session.images()
                .get(RABBITMQ_IMAGE)
                .orElseThrow(() -> new AssertionError("Failed to get the required image"));

            try (var container = image.start()) {
                Eventually.assertThat(container.onStart())
                    .isCompleted();

                Eventually.assertThat(container.pause())
                    .isCompleted();

                Eventually.assertThat(container.unpause())
                    .isCompleted();

                container.remove();
            }
        }
    }

    /**
     * Ensure a {@code Docker} {@link Session} can be used build a new {@link Image} and then remove it.
     */
    @Test
    void shouldBuildAndRemoveANewImageWithATarFile() {

        try (var session = createSession()) {
            // obtain the Path to the example tar
            final var path = Paths.get(ClassLoader.getSystemResource("gdaymate.tar").getPath());

            // attempt to build the image for the tar
            final var image = session.images()
                .build(path)
                .orElseThrow(() -> new AssertionError("Failed to build the Image"));

            // remove the image
            image.remove();

            assertThat(session.images()
                .get(image.id()))
                .isEmpty();
        }
    }

    /**
     * Ensure a {@code Docker} {@link Session} can be used build a new {@link Image} based on a
     * {@link DockerContextBuilder}.
     */
    @Test
    void shouldBuildAndRemoveANewImageWithADockerContextBuilder()
        throws IOException {

        try (var session = createSession()) {

            // establish a DockerContextBuilder from which to create our Docker Context (tar)
            final var builder = new DockerContextBuilder();

            // locate and include the Dockerfile
            final var dockerfile = Paths.get(ClassLoader.getSystemResource("gdaymate/Dockerfile").getFile());

            builder.content().add(dockerfile);

            // create a random name for the Image to create
            final var name = ImageName.of(UUID.randomUUID().toString());

            // attempt to build the image for the tar
            final Image image = session.images()
                .build(builder, name)
                .orElseThrow(() -> new AssertionError("Failed to build the Image"));

            // ensure the Image has the specified name
            assertThat(image.names()
                .findFirst()
                .orElseThrow(() -> new AssertionError("Failed to find the ImageName")))
                .isEqualTo(name.latest());

            // attempt to remove the image
            image.remove();

            assertThat(session.images().get(name.get()))
                .isEmpty();
        }
    }

    /**
     * Ensure a {@code Docker} {@link Session} can be used build a new {@link Image} and start it.
     */
    @Test
    void shouldBuildAndStartANewImageWithATarFile() {

        try (var session = createSession()) {
            // obtain the Path to the example tar
            final var path = Paths.get(ClassLoader.getSystemResource("gdaymate.tar").getPath());

            // attempt to build the image for the tar
            final var image = session.images()
                .build(path)
                .orElseThrow(() -> new AssertionError("Failed to build the Image"));

            // start a container with the image
            try (var container = image.start()) {

                Eventually.assertThat(container.onStart())
                    .isCompleted();

                // assert the expected message is produced by the Terminal
                try (var terminal = container.attach()) {
                    try (var reader = new BufferedReader(terminal.getOutputReader())) {
                        assertThat(reader.readLine())
                            .isEqualTo("G'Day Mate from Docker!");
                    } catch (final IOException e) {
                        throw new AssertionError("Could not read from the Terminal", e);
                    }
                }

                Eventually.assertThat(container.onExit())
                    .isCompleted();

                container.remove();
            } finally {
                image.remove();
            }
        }
    }

    /**
     * Ensure a {@code Docker} {@link Session} can be used to create a custom {@code Dockerfile}, build a custom
     * {@link Image} with it, then use to start and attach to a {@link Container}.
     */
    @Test
    void shouldBuildAndStartAContainerFromACustomImage()
        throws IOException {

        try (var session = createSession()) {

            // establish the custom Dockerfile
            final var message = "Gudday Mate!";
            final var dockerFileBuilder = new DockerFileBuilder()
                .from(ALPINE_IMAGE)
                .command("echo", message);

            // establish the custom Docker Context for the Image (including the custom Dockerfile)
            final var dockerContextBuilder = new DockerContextBuilder();
            dockerContextBuilder.withDockerFile(dockerFileBuilder);

            // attempt to build the custom Image
            final var image = session.images()
                .build(dockerContextBuilder, ImageName.of("custom-image"))
                .orElseThrow(() -> new AssertionError("Failed to build the custom image"));

            try (var container = image.start()) {

                // assert the expected message is produced by the Terminal
                try (var terminal = container.attach()) {
                    try (var reader = new BufferedReader(terminal.getOutputReader())) {
                        assertThat(reader.readLine())
                            .isEqualTo(message);
                    } catch (final IOException e) {
                        throw new AssertionError("Could not read from the Terminal", e);
                    }
                }

                container.remove();
            } finally {
                image.remove();
            }
        }
    }

    /**
     * Ensure files can be copied into a {@link Container}.
     */
    @Test
    void shouldCopyAFileToAContainer()
        throws Exception {

        try (var session = createSession()) {
            final var image = session.images()
                .get(ALPINE_IMAGE)
                .orElseThrow(() -> new AssertionError("Failed to get the required image"));

            try (var container = image.start(Command.of("sleep", "60"))) {

                Eventually.assertThat(container.onStart())
                    .isCompleted();

                assertThat(container.image())
                    .isNotNull();

                assertThat(container.id())
                    .isNotNull();

                // create a /tmp/test folder in the container; withTerminal(true) ensures Detach:false so
                // onExit() tracks actual process completion rather than just exec launch
                final var mkdirExecution = container
                    .createExecutable("mkdir", "/tmp/test")
                    .withTerminal(true)
                    .execute();

                Eventually.assertThat(mkdirExecution.onExit())
                    .isCompleted();

                assertThat(mkdirExecution.exitValue().getAsInt())
                    .isEqualTo(0);

                // establish a temporary folder and a temporary file in the folder, tar the folder and send it to the

                // Create temp dir and a file inside it
                final var tempDirWithPrefix = Files.createTempDirectory("archiveTest");
                tempDirWithPrefix.toFile().deleteOnExit();
                final var testFileName = "test.txt";
                final var testFilePath = tempDirWithPrefix.resolve(testFileName);

                // Write the test file
                final var str = "Hello World";
                try (var writer = new BufferedWriter(new FileWriter(testFilePath.toFile()))) {
                    writer.write(str);
                }

                final var tarFileName = "test.tar";
                final var tarFilePath = tempDirWithPrefix.resolve(tarFileName);

                // Copy a file to the container
                container.copyFiles(tarFilePath, "/tmp/test", testFilePath);

                // Get information about the extracted file and make sure the file was copied over
                final Optional<Map<String, String>> fileInformation = container
                    .fileInformation(Paths.get("/tmp/test/" + testFileName));

                assertThat(fileInformation)
                    .isPresent();

                // If the file was not found in the directory in the container, this will fail
                final Map<String, String> information = fileInformation.get();
                assertThat(information.get("name"))
                    .isEqualTo(testFileName);

                container.kill();
                container.remove();
            }
        }
    }

    /**
     * When creating a new container with a {@link Link}, ensure the links are established.
     */
    @Test
    void shouldLinksBeCreated() {
        try (var session = createSession()) {
            final var image = session.images()
                .get(RABBITMQ_IMAGE)
                .orElseThrow(() -> new AssertionError("Failed to get the required image"));

            final var uniqueNameGenerator = new UniqueNameGenerator(".");

            final var firstContainerName = uniqueNameGenerator.next();
            try (var firstContainer = image.start(ContainerName.of(firstContainerName))) {

                final var secondImage = session.images()
                    .get(ALPINE_IMAGE)
                    .orElseThrow(() -> new AssertionError("Failed to get the required image"));

                final var secondContainerName = uniqueNameGenerator.next();
                try (var secondContainer = secondImage.start(
                    ContainerName.of(secondContainerName),
                    Link.of(firstContainer.id(), firstContainerName))) {

                    final var information = secondContainer.inspect()
                        .orElseThrow();

                    final var name = information.name();
                    assertThat(name)
                        .contains(secondContainerName);

                    assertThat(information.links())
                        .hasSize(1);

                    final var link = information.links()
                        .findFirst()
                        .orElseThrow();

                    assertThat(link.existingNameOrId())
                        .contains(firstContainerName);

                    assertThat(link.nameToLink())
                        .contains(firstContainerName);

                    secondContainer.remove();
                }
                firstContainer.remove();
            }
        }
    }

    /**
     * Should a network be created.
     */
    @Test
    void shouldCreateQueryAndDeleteNetwork() {
        final var networkName = "shouldCreateQueryAndDeleteNetwork";

        try (Session session = createSession()) {
            // defensively delete the network if not cleaned up.
            try {
                session.networks()
                    .delete(networkName);
            } catch (final Exception e) {
                // do Nothing
            }

            final var information = session.networks()
                .create(networkName)
                .orElseThrow(() -> new AssertionError("Failed to create the network"));

            assertThat(information)
                .isNotNull();

            assertThat(information.name())
                .isEqualTo(networkName);

            final var info = session.networks()
                .inspect(networkName)
                .orElseThrow(() -> new AssertionError("Did not find network"));

            assertThat(info)
                .isNotNull();

            assertThat(info.name())
                .isEqualTo(networkName);

            assertThat(session.networks()
                .delete(networkName))
                .isTrue();
        }
    }

    /**
     * Tests creating a container with a network.
     */
    @Test
    void shouldContainerBeCreatedWithANetwork() {
        final var networkName = "shouldContainerBeCreatedWithANetwork";
        try (var session = createSession()) {
            // defensively delete the network if not cleaned up.
            try {
                session.networks()
                    .delete(networkName);
            } catch (final Exception e) {
                // do Nothing
            }

            session.networks()
                .create(networkName)
                .orElseThrow(() -> new AssertionError("Failed to pull the image"));

            final var secondImage = session.images()
                .get(ALPINE_IMAGE)
                .orElseThrow(() -> new AssertionError("Failed to get the required image"));

            try (var container = secondImage.start(NetworkName.of(networkName))) {
                Eventually.assertThat(container.onStart())
                    .isCompleted();

                container.remove();
            }

            // try to start the container on a bad network
            assertThrows(RuntimeException.class, () -> {
                try (var container = secondImage.start(NetworkName.of("invalid"))) {
                    container.remove();
                }
            });
        }
    }
}
