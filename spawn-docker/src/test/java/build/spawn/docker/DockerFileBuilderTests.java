package build.spawn.docker;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link DockerFileBuilder}.
 *
 * @author brian.oliver
 * @since Aug-2021
 */
class DockerFileBuilderTests {

    /**
     * Ensure a {@link DockerFileBuilder} can be created.
     */
    @Test
    void shouldCreateEmptyDockerfileBuilder() {
        new DockerFileBuilder();
    }

    /**
     * Ensure an empty {@link DockerFileBuilder} can be created.
     *
     * @throws IOException should the {@link DockerFileBuilder} fail to create the {@code Dockerfile}
     */
    @Test
    void shouldCreateEmptyDockerfile()
        throws IOException {

        final DockerFileBuilder builder = new DockerFileBuilder();

        final Path path = builder.build();

        assertThat(Files.exists(path))
            .isTrue();
    }

    /**
     * Ensure a simple "hello world" {@code Dockerfile} can be built with a {@link DockerFileBuilder}.
     *
     * @throws IOException should the {@link DockerFileBuilder} fail to create the {@code Dockerfile}
     */
    @Test
    void shouldCreateDockerfile()
        throws IOException {

        final StringWriter writer = new StringWriter();

        final DockerFileBuilder builder = new DockerFileBuilder()
            .from("alpine:latest")
            .copyInto(Paths.get("/usr/local/"), Paths.get("/tmp/local/echo"))
            .copyInto("/usr/local", "/tmp/bin/*")
            .workingDirectory("/usr/local")
            .command("echo", "Hello World");

        builder.build(writer);

        assertThat(writer.toString())
            .isEqualTo("FROM alpine:latest\n"
                + "COPY [\"/tmp/local/echo\", \"/usr/local\"]\n"
                + "COPY [\"/tmp/bin/*\", \"/usr/local\"]\n"
                + "WORKDIR /usr/local\n"
                + "CMD [\"echo\", \"Hello World\"]\n");
    }
}
