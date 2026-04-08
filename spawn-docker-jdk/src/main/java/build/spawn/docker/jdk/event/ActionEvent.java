package build.spawn.docker.jdk.event;

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

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents an immutable {@code Docker Engine} Action Event.
 *
 * @author brian.oliver
 * @since Apr-2026
 */
public class ActionEvent
    extends AbstractEvent {

    /**
     * Obtains the type of the event.
     *
     * @return the type of the event
     */
    public String type() {
        return jsonNode().get("Type").asText();
    }

    /**
     * Obtains the action.
     *
     * @return the action
     */
    public String action() {
        return jsonNode().get("Action").asText();
    }

    /**
     * Obtains the Actor {@link JsonNode}.
     *
     * @return the Actor {@link JsonNode}
     */
    public JsonNode actor() {
        return jsonNode().get("Actor");
    }
}
