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
import build.spawn.docker.Container;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Objects;

/**
 * A {@link DockerOption} to bind a {@link Path} outside a {@link Container} inside a {@link Container}.
 *
 * @author brian.oliver
 * @since Aug-2021
 */
public class Bind
    implements DockerOption, CollectedOption<LinkedHashSet> {

    /**
     * The external {@link Path} to bind.
     */
    private final Path externalPath;

    /**
     * The internal {@link Path} to bind.
     */
    private final Path internalPath;

    /**
     * Constructs a {@link Bind}.
     *
     * @param externalPath the external {@link Path}
     * @param internalPath the external {@link Path}
     */
    private Bind(final Path externalPath,
                 final Path internalPath) {

        Objects.requireNonNull(externalPath, "The external Path must not be null");
        Objects.requireNonNull(externalPath, "The internal Path must not be null");

        this.externalPath = externalPath;
        this.internalPath = internalPath;
    }

    @Override
    public void configure(final ObjectNode objectNode, final ObjectMapper objectMapper) {

        // ensure the "HostConfig" exists an ObjectNode
        final ObjectNode hostConfig = objectNode.get("HostConfig") == null
            || !(objectNode.get("HostConfig") instanceof ObjectNode)
            ? objectMapper.createObjectNode()
            : (ObjectNode) objectNode.get("HostConfig");

        // ensure the "Binds" exists as an ObjectNode
        final ArrayNode binds = hostConfig.get("Binds") == null
            || !(hostConfig.get("Binds") instanceof ArrayNode)
            ? objectMapper.createArrayNode()
            : (ArrayNode) hostConfig.get("Binds");

        binds.add(this.externalPath.toString() + ":" + this.internalPath.toString());

        hostConfig.set("Binds", binds);
        objectNode.set("HostConfig", hostConfig);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        final Bind bind = (Bind) object;
        return this.externalPath.equals(bind.externalPath) && this.internalPath.equals(bind.internalPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.externalPath, this.internalPath);
    }

    @Override
    public String toString() {
        return "Bind{" + this.externalPath + ":" + this.internalPath + '}';
    }

    /**
     * Create a {@link Bind} for a specified {@link Path}.
     *
     * @param path the {@link Path}
     * @return a {@link Bind}
     */
    public static Bind of(final Path path) {
        return new Bind(path, path);
    }

    /**
     * Create a {@link Bind} for a specified {@link Path}.
     *
     * @param externalPath the external {@link Path}
     * @param internalPath the external {@link Path}
     * @return a {@link Bind}
     */
    public static Bind of(final Path externalPath, final Path internalPath) {
        return new Bind(externalPath, internalPath);
    }
}
