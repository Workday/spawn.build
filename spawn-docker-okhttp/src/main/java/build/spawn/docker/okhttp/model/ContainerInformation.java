package build.spawn.docker.okhttp.model;

/*-
 * #%L
 * Spawn Docker (OkHttp Client)
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

import build.spawn.docker.Container;
import build.spawn.docker.Image;
import build.spawn.docker.PublishedPort;
import build.spawn.docker.option.ExposedPort;
import build.spawn.docker.option.Link;
import jakarta.inject.Inject;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;

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
        return jsonNode().get("Name").asText();
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
        return StreamSupport
            .stream(
                Spliterators.spliteratorUnknownSize(
                    jsonNode().at("/NetworkSettings/Ports").fields(),
                    Spliterator.IMMUTABLE),
                false)
            .map(entry ->
                ExposedPort.of(entry.getKey())
                    .map(exposedPort -> PublishedPort.of(exposedPort,
                        StreamSupport.stream(
                                Spliterators.spliteratorUnknownSize(entry.getValue().iterator(), Spliterator.IMMUTABLE)
                                , false)
                            .map(address -> new InetSocketAddress(address.get("HostIp").asText(),
                                address.get("HostPort").asInt()))))
                    .orElse(null))
            .filter(Objects::nonNull);
    }

    @Override
    public String ipAddress() {
        final JsonNode node = jsonNode().at("/NetworkSettings");
        return node.get("IPAddress").asText();
    }

    @Override
    public long pid() {
        return jsonNode().at("/State/Pid").asInt(-1);
    }

    @Override
    public Optional<Container.State> state() {
        try {
            return Optional.of(Enum.valueOf(
                Container.State.class,
                jsonNode().at("/State/Status").asText().toUpperCase()));
        }
        catch (final Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Stream<Link> links() {
        final JsonNode links = jsonNode().at("/HostConfig/Links");
        final Iterator<JsonNode> iterator = links.iterator();
        final List<Link> linkList = new ArrayList<>();
        while (iterator.hasNext()) {
            final JsonNode entry = iterator.next();
            final String text = entry.asText();
            final String[] split = text.split(":");
            linkList.add(Link.of(split[0], split[1]));
        }

        return linkList.stream();
    }
}
