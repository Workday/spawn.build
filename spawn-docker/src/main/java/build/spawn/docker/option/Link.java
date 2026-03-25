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

import java.util.LinkedHashSet;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A {@link DockerOption} to link an existing container name or id to the new container.
 *
 * @author anand.sankaran
 * @since Aug-2022
 */
public class Link
    implements DockerOption, CollectedOption<LinkedHashSet> {

    /**
     * The current container name or id.
     */
    private final String existingNameOrId;

    /**
     * The name to link to.
     */
    private final String nameToLink;

    /**
     * Constructs a {@link Link}.
     *
     * @param existingNameOrId existing container name or id
     * @param nameToLink       the name to link
     */
    private Link(final String existingNameOrId,
                 final String nameToLink) {

        Objects.requireNonNull(existingNameOrId, "The existing name or id must not be null");
        Objects.requireNonNull(nameToLink, "The name to link must not be null");

        this.existingNameOrId = existingNameOrId;
        this.nameToLink = nameToLink;
    }

    @Override
    public void configure(final ObjectNode objectNode, final ObjectMapper objectMapper) {

        // ensure the "HostConfig" exists an ObjectNode
        final ObjectNode hostConfig = objectNode.get("HostConfig") == null
            || !(objectNode.get("HostConfig") instanceof ObjectNode)
            ? objectMapper.createObjectNode()
            : (ObjectNode) objectNode.get("HostConfig");

        // ensure the "Links" exists as an ObjectNode
        final ArrayNode links = hostConfig.get("Links") == null
            || !(hostConfig.get("Links") instanceof ArrayNode)
            ? objectMapper.createArrayNode()
            : (ArrayNode) hostConfig.get("Links");

        links.add(this.existingNameOrId + ":" + this.nameToLink);

        hostConfig.set("Links", links);
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
        final Link link = (Link) object;
        return this.existingNameOrId.equals(link.existingNameOrId) && this.nameToLink.equals(link.nameToLink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.existingNameOrId, this.nameToLink);
    }

    @Override
    public String toString() {
        return "Link{" + this.existingNameOrId + ":" + this.nameToLink + '}';
    }

    /**
     * Create a {@link Link} between existing container name or id and the name to be linked.
     *
     * @param existingNameOrId the existing name or id to link
     * @param nameToLink       the name to link
     * @return a {@link Link}
     */
    public static Link of(final String existingNameOrId, final String nameToLink) {
        return new Link(existingNameOrId, nameToLink);
    }

    /**
     * Gets the existing name or id.
     *
     * @return the existing name or id
     */
    public String existingNameOrId() {
        return this.existingNameOrId;
    }

    /**
     * Gets the name to be linked.
     *
     * @return the name to be linked
     */
    public String nameToLink() {
        return this.nameToLink;
    }
}
