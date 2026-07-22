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
import build.base.json.Json;
import build.base.json.JsonArray;
import build.base.json.JsonObject;
import build.base.json.JsonValue;
import build.base.naming.UniqueNameGenerator;
import build.base.option.HostName;
import build.codemodel.dependency.injection.Context;
import build.spawn.docker.Container;
import build.spawn.docker.Image;
import build.spawn.docker.jdk.HttpTransport;
import build.spawn.docker.jdk.model.DockerContainer;
import build.spawn.docker.option.Bind;
import build.spawn.docker.option.ContainerName;
import build.spawn.docker.option.DockerOption;
import build.spawn.docker.option.ExposedPort;
import build.spawn.docker.option.ExtraHost;
import build.spawn.docker.option.Link;
import build.spawn.docker.option.NetworkName;
import build.spawn.docker.option.PublishAllPorts;
import build.spawn.docker.option.PublishPort;
import build.spawn.option.EnvironmentVariable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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

        final var nodeBuilder = JsonObject.builder();
        nodeBuilder.put("Image", this.image.id());

        this.configuration.getOptionalValue(HostName.class)
            .ifPresent(h -> nodeBuilder.put("Hostname", h));

        final var envList = this.configuration.stream(EnvironmentVariable.class)
            .map(e -> e.key() + "=" + e.value().orElse(""))
            .toList();
        if (!envList.isEmpty()) {
            nodeBuilder.put("Env", JsonArray.builder().addAll(envList).build());
        }

        // Accumulate each DockerOption type; the exhaustive switch enforces that new
        // permitted subtypes are handled here at compile time.
        final var cmdList = new ArrayList<String>();
        final var exposedPortList = new ArrayList<ExposedPort>();
        final var bindList = new ArrayList<Bind>();
        final var linkList = new ArrayList<Link>();
        final var extraHostList = new ArrayList<ExtraHost>();
        final var publishPortList = new ArrayList<PublishPort>();
        PublishAllPorts publishAllPorts = null;

        for (final DockerOption option : this.configuration.stream(DockerOption.class).toList()) {
            var _ = switch (option) {
                case build.spawn.docker.option.Command c -> cmdList.addAll(c.values());
                case ExposedPort ep -> exposedPortList.add(ep);
                case Bind b -> bindList.add(b);
                case Link l -> linkList.add(l);
                case ExtraHost h -> extraHostList.add(h);
                case PublishPort pp -> publishPortList.add(pp);
                case PublishAllPorts p -> {
                    publishAllPorts = p;
                    yield true;
                }
            };
        }

        if (!cmdList.isEmpty()) {
            nodeBuilder.put("Cmd", JsonArray.builder().addAll(cmdList).build());
        }

        if (!exposedPortList.isEmpty()) {
            final var obj = JsonObject.builder();
            exposedPortList.forEach(ep ->
                obj.put(ep.port() + "/" + ep.type().toString().toLowerCase(), JsonObject.builder().build()));
            nodeBuilder.put("ExposedPorts", obj.build());
        }

        final var hostConfigBuilder = JsonObject.builder();

        if (!bindList.isEmpty()) {
            hostConfigBuilder.put("Binds", JsonArray.builder().addAll(
                bindList.stream().map(b -> b.externalPath() + ":" + b.internalPath()).toList()).build());
        }

        if (!linkList.isEmpty()) {
            hostConfigBuilder.put("Links", JsonArray.builder().addAll(
                linkList.stream().map(l -> l.existingNameOrId() + ":" + l.nameToLink()).toList()).build());
        }

        if (!extraHostList.isEmpty()) {
            hostConfigBuilder.put("ExtraHosts", JsonArray.builder().addAll(
                extraHostList.stream().map(ExtraHost::get).toList()).build());
        }

        if (!publishPortList.isEmpty()) {
            final var grouped = new LinkedHashMap<String, ArrayList<PublishPort>>();
            publishPortList.forEach(pb -> {
                final String key = pb.port() + "/" + pb.type().toString().toLowerCase();
                grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(pb);
            });
            final var portBindingsObj = JsonObject.builder();
            grouped.forEach((key, pbs) -> {
                final var arr = JsonArray.builder();
                for (final var pb : pbs) {
                    final var addr = pb.getSocketAddress()
                        .orElse(new InetSocketAddress("localhost", pb.port()));
                    final var pbObj = JsonObject.builder()
                        .put("HostPort", Integer.toString(addr.getPort()));
                    if (!addr.getHostName().equals("localhost")) {
                        pbObj.put("HostIp", addr.getHostName());
                    }
                    arr.add(pbObj.build());
                }
                portBindingsObj.put(key, arr.build());
            });
            hostConfigBuilder.put("PortBindings", portBindingsObj.build());
        }

        if (publishAllPorts != null) {
            hostConfigBuilder.put("PublishAllPorts", publishAllPorts.isEnabled());
        }

        this.configuration.stream(NetworkName.class)
            .findFirst()
            .ifPresent(n -> hostConfigBuilder.put("NetworkMode", n.get()));

        final var hostConfig = hostConfigBuilder.build();
        if (!hostConfig.members().isEmpty()) {
            nodeBuilder.put("HostConfig", hostConfig);
        }

        final var name = this.configuration.getOptionalValue(ContainerName.class)
            .orElse(new UniqueNameGenerator(".").next());

        return HttpTransport.Request
            .post("/containers/create?name=" + name,
                nodeBuilder.build().toJsonString().getBytes(StandardCharsets.UTF_8))
            .withContentType("application/json");
    }

    @Override
    protected Container createResult(final HttpTransport.Response response)
        throws IOException {

        final var context = createContext();
        context.bind(Image.class).to(this.image);
        context.bind(Context.class).to(context);
        context.bind(Configuration.class).to(this.configuration);

        // bind the JsonValue representation of the response
        context.bind(JsonValue.class).to(Json.parse(response.bodyString()));

        // create the Container to return
        return context.create(DockerContainer.class);
    }
}
