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

import build.base.configuration.AbstractValueOption;
import build.base.configuration.CollectedOption;

import java.util.LinkedHashSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A {@link DockerOption} to define an extra {@code /etc/host} information for a {@link build.spawn.docker.Container}
 * when it's being <a href="https://docs.docker.com/engine/api/v1.41/#operation/ContainerCreate">created</a>.
 *
 * @author brian.oliver
 * @since Aug-2021
 */
public class ExtraHost
    extends AbstractValueOption<String>
    implements DockerOption, CollectedOption<LinkedHashSet> {

    /**
     * Constructs the {@link ExposedPort} for the specified extra host.
     *
     * @param host the host
     */
    private ExtraHost(final String host) {
        super(host);
    }

    /**
     * Create an {@link ExtraHost} for the specified host.
     *
     * @param host the host
     * @return a new {@link ExtraHost}
     */
    public static ExtraHost of(final String host) {
        return new ExtraHost(host);
    }

    @Override
    public void configure(final ObjectNode objectNode, final ObjectMapper objectMapper) {

        // ensure the "/HostConfig" exists
        final ObjectNode hostConfig = objectNode.get("HostConfig") == null
            ? objectMapper.createObjectNode()
            : (ObjectNode) objectNode.get("HostConfig");

        objectNode.set("HostConfig", hostConfig);

        // ensure "/HostConfig/ExtraHosts" exists
        final ArrayNode extraHosts = hostConfig.get("ExtraHosts") == null
            ? objectMapper.createArrayNode()
            : (ArrayNode) hostConfig.get("ExtraHosts");

        hostConfig.set("ExtraHosts", extraHosts);

        // add this ExtraHost
        extraHosts.add(get());
    }
}
