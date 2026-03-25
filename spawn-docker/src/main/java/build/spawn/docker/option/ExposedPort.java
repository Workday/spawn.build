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
import build.base.foundation.Strings;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A {@link DockerOption} to expose a port for a {@link build.spawn.docker.Container}.
 *
 * @author brian.oliver
 * @since Aug-2021
 */
public final class ExposedPort
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
     * Constructs an {@link ExposedPort}.
     *
     * @param port the port
     * @param type the {@link Type}
     */
    private ExposedPort(final int port, final Type type) {
        this.port = port;
        this.type = type == null ? Type.TCP : type;
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ExposedPort that = (ExposedPort) o;
        return this.port == that.port && this.type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.port, this.type);
    }

    @Override
    public String toString() {
        return "ExposedPort{" + this.port + '/' + this.type.toString().toLowerCase() + '}';
    }

    @Override
    public void configure(final ObjectNode objectNode, final ObjectMapper objectMapper) {

        // ensure the "ExposedPorts" exists as an ArrayNode
        final ObjectNode exposedPorts = objectNode.get("ExposedPorts") == null
            || !(objectNode.get("ExposedPorts") instanceof ObjectNode)
            ? objectMapper.createObjectNode()
            : (ObjectNode) objectNode.get("ExposedPorts");

        // add the ExposedPort
        exposedPorts.set(this.port + "/" + this.type.toString().toLowerCase(), objectMapper.createObjectNode());

        objectNode.set("ExposedPorts", exposedPorts);
    }

    /**
     * Creates a {@link Type#TCP} exposed port.
     *
     * @param port the port to expose
     * @return an {@link ExposedPort}
     */
    public static ExposedPort of(final int port) {
        return new ExposedPort(port, Type.TCP);
    }

    /**
     * Creates an exposed port of a specific {@link Type}.
     *
     * @param port the port to expose
     * @param type the type to expose
     * @return an {@link ExposedPort}
     */
    public static ExposedPort of(final int port, final Type type) {
        return new ExposedPort(port, type);
    }

    /**
     * Attempts to create an {@link ExposedPort} given the {@link String} representation of one, in the format
     * {@code port} or {@code port/type}, the former assumed to be a {@link Type#TCP} port.
     *
     * @param string the {@link String} representation of the exposed port
     * @return the {@link Optional} {@link ExposedPort} or {@link Optional#empty()} should the port not be obtainable
     */
    public static Optional<ExposedPort> of(final String string) {
        if (Strings.isEmpty(string)) {
            return Optional.empty();
        }

        final String[] parts = string.split("/");

        if (parts.length == 0 || parts.length > 2) {
            return Optional.empty();
        }

        try {
            final int port = Integer.parseInt(parts[0]);

            final Type type = parts.length == 1
                ? Type.TCP
                : Enum.valueOf(Type.class, parts[1].toUpperCase());

            return Optional.of(ExposedPort.of(port, type));
        }
        catch (final Exception e) {
            return Optional.empty();
        }
    }

    /**
     * A type of {@link ExposedPort}.
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
