package build.spawn.docker.option;

/*-
 * #%L
 * Spawn Docker (Client)
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

import build.base.configuration.CollectedOption;

import java.net.InetSocketAddress;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A {@link DockerOption} to publish an internal {@link build.spawn.docker.Container} port to an external
 * (outside the {@link build.spawn.docker.Container}) {@link InetSocketAddress} on the host that is hosting the said
 * {@link build.spawn.docker.Container}.
 *
 * @author brian.oliver
 * @since Aug-2022
 */
public final class PublishPort
    implements DockerOption, CollectedOption<LinkedHashSet> {

    /**
     * The port.
     */
    private final int port;

    /**
     * The {@link Type} of port to expose.
     */
    private final Type type;

    /**
     * The {@link Optional} external {@link InetSocketAddress}.
     */
    private final Optional<InetSocketAddress> socketAddress;

    /**
     * Constructs an {@link PublishPort}.
     *
     * @param port          the port
     * @param type          the {@link Type}
     * @param socketAddress the {@code null}able {@link InetSocketAddress}
     */
    private PublishPort(final int port,
                        final Type type,
                        final InetSocketAddress socketAddress) {

        this.port = port;
        this.type = type == null ? Type.TCP : type;
        this.socketAddress = Optional.ofNullable(socketAddress);
    }

    /**
     * Obtains the port to expose.
     *
     * @return the port
     */
    public int port() {
        return this.port;
    }

    /**
     * Obtains the {@link Type} of port to expose.
     *
     * @return the {@link Type}
     */
    public Type type() {
        return this.type;
    }

    /**
     * Obtains the {@link Optional} external {@link InetSocketAddress} to which to publish the
     * internal {@link build.spawn.docker.Container} port.
     *
     * @return the {@link Optional} {@link InetSocketAddress}
     */
    public Optional<InetSocketAddress> getSocketAddress() {
        return this.socketAddress;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PublishPort that = (PublishPort) o;
        return this.port == that.port && this.type == that.type && this.socketAddress.equals(that.socketAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.port, this.type, this.socketAddress);
    }

    @Override
    public String toString() {
        return "PortBinding{"
            + this.port + '/'
            + this.type.toString().toLowerCase()
            + this.socketAddress
            + '}';
    }

    @Override
    public void configure(final ObjectNode objectNode, final ObjectMapper objectMapper) {

        // ensure the "HostConfig" exists an ObjectNode
        final ObjectNode hostConfig = objectNode.get("HostConfig") == null
            || !(objectNode.get("HostConfig") instanceof ObjectNode)
            ? objectMapper.createObjectNode()
            : (ObjectNode) objectNode.get("HostConfig");

        // ensure the "PortBindings" exists as an ObjectNode
        final ObjectNode portBindings = hostConfig.get("PortBindings") == null
            || !(hostConfig.get("PortBindings") instanceof ObjectNode)
            ? objectMapper.createObjectNode()
            : (ObjectNode) hostConfig.get("PortBindings");

        final String key = this.port + "/" + this.type.toString().toLowerCase();

        // ensure the PortBinding exists as an ArrayNode
        final ArrayNode array = portBindings.get(key) == null
            || !(portBindings.get(key) instanceof ArrayNode)
            ? objectMapper.createArrayNode()
            : (ArrayNode) portBindings.get(key);

        // create the PortBinding
        final ObjectNode portBinding = objectMapper.createObjectNode();
        final InetSocketAddress address = this.socketAddress
            .orElse(new InetSocketAddress("localhost", this.port));

        if (!address.getHostName().equals("localhost")) {
            portBinding.put("HostIp", address.getHostName());
        }

        portBinding.put("HostPort", Integer.toString(address.getPort()));

        array.add(portBinding);

        portBindings.set(key, array);
        hostConfig.set("PortBindings", portBindings);
        objectNode.set("HostConfig", hostConfig);
    }

    /**
     * Creates a {@link Type#TCP} exposed port.
     *
     * @param internalPort the port to expose
     * @return an {@link PublishPort}
     */
    public static PublishPort of(final int internalPort) {
        return new PublishPort(internalPort, Type.TCP, null);
    }

    /**
     * Creates an exposed port of a specific {@link Type}.
     *
     * @param internalPort the port to expose
     * @param type         the type to expose
     * @return an {@link PublishPort}
     */
    public static PublishPort of(final int internalPort, final Type type) {
        return new PublishPort(internalPort, type, null);
    }

    /**
     * Creates an exposed port of a specific {@link Type}.
     *
     * @param internalPort the port to expose
     * @param type         the type to expose
     * @param externalPort the external port
     * @return an {@link PublishPort}
     */
    public static PublishPort of(final int internalPort,
                                 final Type type,
                                 final int externalPort) {
        return new PublishPort(internalPort, type, new InetSocketAddress("localhost", externalPort));
    }

    /**
     * A type of {@link PublishPort}.
     */
    public enum Type {
        /**
         * TCP port.
         */
        TCP,

        /**
         * UDP port.
         */
        UDP,

        /**
         * SCTP port.
         */
        SCTP
    }
}
