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

import build.base.configuration.Default;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A {@link DockerOption} to publish {@link ExposedPort}s for a {@link build.spawn.docker.Container}.
 *
 * @author brian.oliver
 * @since Aug-2021
 */
public enum PublishAllPorts
    implements DockerOption {

    /**
     * Enabled publishing all {@link ExposedPort}s for a {@link build.spawn.docker.Container}.
     */
    @Default
    ENABLED,

    /**
     * Disables publishing of {@link ExposedPort}s.
     */
    DISABLED;

    @Override
    public void configure(final ObjectNode objectNode, final ObjectMapper objectMapper) {

        // ensure the "HostConfig" exists an ObjectNode
        final ObjectNode hostConfig = objectNode.get("HostConfig") == null
            || !(objectNode.get("HostConfig") instanceof ObjectNode)
            ? objectMapper.createObjectNode()
            : (ObjectNode) objectNode.get("HostConfig");

        hostConfig.put("PublishAllPorts", this == ENABLED);

        objectNode.set("HostConfig", hostConfig);
    }
}
