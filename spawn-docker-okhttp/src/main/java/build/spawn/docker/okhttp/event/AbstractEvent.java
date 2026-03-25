package build.spawn.docker.okhttp.event;

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

import build.spawn.docker.Event;
import build.spawn.docker.okhttp.model.AbstractJsonBasedResult;

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * An abstract {@link Event}.
 *
 * @author brian.oliver
 * @since Jun-2021
 */
public abstract class AbstractEvent
    extends AbstractJsonBasedResult
    implements Event {

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(final Class<T> requiredClass) {
        return requiredClass != null
            && requiredClass.isAssignableFrom(JsonNode.class)
            ? Optional.of((T) jsonNode())
            : Event.super.get(requiredClass);
    }
}
