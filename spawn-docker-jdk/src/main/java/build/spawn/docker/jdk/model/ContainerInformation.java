package build.spawn.docker.jdk.model;

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

import build.base.json.JsonArray;
import build.base.json.JsonString;
import build.base.json.JsonValue;
import build.spawn.docker.Container;
import build.spawn.docker.Image;
import build.spawn.docker.PublishedPort;
import build.spawn.docker.option.ExposedPort;
import build.spawn.docker.option.Link;
import jakarta.inject.Inject;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * An internal implementation of {@link Container.Information}.
 *
 * @author brian.oliver
 * @since Aug-2021
 */
public class ContainerInformation
    extends AbstractJsonBasedResult
    implements Container.Information {

    @Inject
    private Container container;

    @Override
    public String containerId() {
        return this.container.id();
    }

    @Override
    public String name() {
        return text("Name");
    }

    @Override
    public Stream<ExposedPort> exposedPorts() {
        return this.container.image().inspect()
            .map(Image.Information::exposedPorts)
            .map(exposedPorts -> Stream.concat(exposedPorts, this.container.configuration().stream(ExposedPort.class)))
            .orElse(Stream.empty());
    }

    @Override
    public Stream<PublishedPort> publishedPorts() {
        final var portsNode = at("NetworkSettings", "Ports");
        if (!(portsNode instanceof build.base.json.JsonObject portsObj)) {
            return Stream.empty();
        }
        return portsObj.members().entrySet().stream()
            .filter(entry -> entry.getValue() instanceof JsonArray)
            .map(entry ->
                ExposedPort.of(entry.getKey())
                    .map(exposedPort -> PublishedPort.of(exposedPort,
                        ((JsonArray) entry.getValue()).values().stream()
                            .map(address -> new InetSocketAddress(
                                address.getString("HostIp"),
                                Integer.parseInt(address.getString("HostPort"))))))
                    .orElse(null))
            .filter(Objects::nonNull);
    }

    @Override
    public String ipAddress() {
        return text("NetworkSettings", "IPAddress");
    }

    @Override
    public long pid() {
        return longAt(-1L, "State", "Pid");
    }

    @Override
    public Optional<Container.State> state() {
        try {
            return Optional.of(Enum.valueOf(
                Container.State.class,
                text("State", "Status").toUpperCase()));
        }
        catch (final Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Stream<Link> links() {
        final var linksNode = at("HostConfig", "Links");
        if (!(linksNode instanceof JsonArray linksArray)) {
            return Stream.empty();
        }
        final List<Link> linkList = new ArrayList<>();
        for (final JsonValue entry : linksArray.values()) {
            final String text = entry instanceof JsonString s ? s.value() : entry.toJsonString();
            final String[] split = text.split(":");
            linkList.add(Link.of(split[0], split[1]));
        }

        return linkList.stream();
    }
}
