package build.spawn.docker.jdk.command;

/*-
 * #%L
 * Spawn Docker (JDK Client)
 * %%
 * Copyright (C) 2026 Workday, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import build.base.configuration.Configuration;
import build.base.naming.UniqueNameGenerator;
import build.base.option.HostName;
import build.codemodel.injection.Context;
import build.spawn.docker.Container;
import build.spawn.docker.Image;
import build.spawn.docker.jdk.HttpTransport;
import build.spawn.docker.jdk.model.DockerContainer;
import build.spawn.docker.option.ContainerName;
import build.spawn.docker.option.DockerOption;
import build.spawn.docker.option.ExposedPort;
import build.spawn.docker.option.NetworkName;
import build.spawn.docker.option.PublishAllPorts;
import build.spawn.docker.option.PublishPort;
import build.spawn.option.EnvironmentVariable;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * The {@code Docker Daemon} {@link Command} to create a {@code Container} for an {@link Image} using the
 * <a href="https://docs.docker.com/engine/api/v1.41/#operation/ContainerCreate">Create Container</a> command.
 *
 * @author brian.oliver
 * @since Jun-2021
 */
public class CreateContainer
    extends AbstractBlockingCommand<Container> {

    /**
     * The {@link ObjectMapper} for parsing json.
     */
    @Inject
    private ObjectMapper objectMapper;

    /**
     * The {@link Image} for which to create the {@link Container}.
     */
    private final Image image;

    /**
     * The {@link Configuration} to create the {@link Container}.
     */
    private final Configuration configuration;

    /**
     * Constructs a {@link CreateContainer} {@link Command}.
     *
     * @param image         the {@link Image} for which the {@link Container} will be created
     * @param configuration the {@link Configuration} for creating the {@link Container}
     */
    public CreateContainer(final Image image,
                           final Configuration configuration) {

        this.image = Objects.requireNonNull(image, "The Image must not be null");
        this.configuration = configuration == null
            ? Configuration.empty()
            : configuration;

        // by default, PublishAllPorts ensure all ExposedPorts are Published
        if (this.configuration.stream(ExposedPort.class)
            .findFirst()
            .isPresent()
            && this.configuration.stream(PublishPort.class)
            .findFirst()
            .isEmpty()) {

            this.configuration.get(PublishAllPorts.class);
        }
    }

    @Override
    protected HttpTransport.Request createRequest() {

        // establish an ObjectNode containing the containers/create json
        final var node = this.objectMapper.createObjectNode();
        node.put("Image", this.image.id());

        // allow the DockerOptions to configure the ObjectNode
        this.configuration.stream(DockerOption.class)
            .forEach(option -> option.configure(node, this.objectMapper));

        // configure docker environment variables node
        this.configuration.stream(EnvironmentVariable.class)
            .forEach(envVar -> {
                final ArrayNode arrayNode = getOrCreateArray(node, "Env");
                arrayNode.add(envVar.key() + "=" + envVar.value().orElse(""));
            });

        // configure network
        this.configuration.stream(NetworkName.class)
            .findFirst()
            .ifPresent(networkName -> {
                ObjectNode hostConfig = (ObjectNode) node.get("HostConfig");
                if (hostConfig == null) {
                    hostConfig = this.objectMapper.createObjectNode();
                    node.set("HostConfig", hostConfig);
                }
                hostConfig.put("NetworkMode", networkName.get());
            });

        // perform custom configuration (for non-DockerOptions)
        this.configuration.getOptionalValue(HostName.class)
            .ifPresent(hostName -> {
                node.put("Hostname", hostName);
            });

        final var name = this.configuration.getOptionalValue(ContainerName.class)
            .orElse(new UniqueNameGenerator(".").next());

        return HttpTransport.Request
            .post("/containers/create?name=" + name,
                node.toString().getBytes(StandardCharsets.UTF_8))
            .withContentType("application/json");
    }

    private ArrayNode getOrCreateArray(final ObjectNode node, final String key) {
        final var child = node.get(key);
        if (child instanceof ArrayNode) {
            return (ArrayNode) child;
        }
        return node.putArray(key);
    }

    @Override
    protected Container createResult(final HttpTransport.Response response)
        throws IOException {

        final var context = createContext();
        context.bind(Image.class).to(this.image);
        context.bind(Context.class).to(context);
        context.bind(Configuration.class).to(this.configuration);

        // bind the JsonNode representation of the response
        final var json = response.bodyString();
        context.bind(JsonNode.class).to(this.objectMapper.readTree(json));

        // create the Container to return
        return context.create(DockerContainer.class);
    }
}
