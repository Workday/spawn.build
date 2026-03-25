package build.spawn.platform.local.jdk;

import build.base.archiving.JarBuilder;
import build.base.assertion.Eventually;
import build.base.network.Network;
import build.base.network.Server;
import build.spawn.application.Console;
import build.spawn.jdk.JDKApplication;
import build.spawn.jdk.agent.SpawnAgent;
import build.spawn.jdk.agent.SpawnAgentArchiveBuilder;
import build.spawn.jdk.option.AddModules;
import build.spawn.jdk.option.ClassPath;
import build.spawn.jdk.option.JDKAgent;
import build.spawn.jdk.option.MainClass;
import build.spawn.jdk.option.ModulePath;
import build.spawn.platform.local.LocalMachine;
import build.spawn.platform.local.jdk.application.SpawnAgentDiagnosticApplication;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for the {@link SpawnAgent}.
 *
 * @author brian.oliver
 * @since Nov-2018
 */
class SpawnAgentTests {

    /**
     * Ensure the {@link SpawnAgent} can be launched.
     *
     * @param temporaryFolder an injected temporary folder
     * @throws IOException        should the {@link Server} fail to start
     * @throws URISyntaxException should the {@link SpawnAgent} URI be invalid
     */
    @Test
    void shouldLaunchSpawnAgent(@TempDir final Path temporaryFolder)
        throws IOException, URISyntaxException {

        // create the SpawnAgent archive
        final var spawnAgentPath = SpawnAgentArchiveBuilder.createArchive();

        // establish a Server to which the SpawnAgent can connect
        try (var server = new Server(0).start()) {

            final var machine = LocalMachine.get();

            // find the first address of the machine that's of the same class as the ServerSocket
            // (ensures that if the ServerSocket is using IPv4, the address returned is IPv4)
            final var inetAddress = machine.addresses()
                .filter(Network.isOfClass(server.getLocalAddress()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Failed to find a local address on the LocalMachine"));

            final var uri = new URI("spawn", null, inetAddress.getHostAddress(), server.getLocalPort(), null, null,
                null);

            // create a Jar containing only the SpawnAgentDiagnosticApplication
            final var jarBuilder = new JarBuilder();

            jarBuilder.content()
                .add(SpawnAgentDiagnosticApplication.class);

            jarBuilder.withManifestVersion("1.0.0")
                .withImplementationTitle("Implementation")
                .withImplementationVendor("build.spawn.platform")
                .withImplementationVersion("1.0.0")
                .withSignatureVersion("1.0.0")
                .withSpecificationVendor("build.spawn.platform")
                .withSpecificationVersion("1.0.0")
                .withSpecificationTitle("Specification");

            final File archive = temporaryFolder
                .resolve("diagnostics.jar")
                .toFile();

            assertThat(archive.exists())
                .isFalse();

            jarBuilder.build(archive.toPath());

            assertThat(archive.exists())
                .isTrue();

            try (var application = machine.launch(JDKApplication.class,
                JDKAgent.of(spawnAgentPath.toString(), "machine=" + uri + ",launchId=0"),
                ClassPath.of(archive.toPath()),
                ModulePath.empty(),
                AddModules.empty(),
                MainClass.of(SpawnAgentDiagnosticApplication.class),
                Console.ofSystem())) {

                Eventually.assertThat(application.onExit())
                    .isCompleted();

                final var exitValue = application.exitValue();

                assertThat(exitValue)
                    .isPresent();

                assertThat(exitValue.getAsInt())
                    .isEqualTo(42);
            }
        }
    }
}
