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

import build.base.json.JsonBoolean;
import build.base.json.JsonNull;
import build.base.json.JsonNumber;
import build.base.json.JsonObject;
import build.base.json.JsonString;
import build.base.json.JsonValue;
import build.spawn.docker.Session;
import build.spawn.docker.jdk.command.Command;
import jakarta.inject.Inject;

/**
 * An abstract {@link JsonValue}-based result produced by a {@link Command}.
 *
 * @author brian.oliver
 * @since Jun-2021
 */
public abstract class AbstractJsonBasedResult {

    /**
     * The {@link Session} that created the request.
     */
    @Inject
    private Session session;

    @Inject
    private JsonValue jsonValue;

    /**
     * Obtains the {@link Session} that executed the {@link Command}.
     *
     * @return the {@link Session}
     */
    protected Session session() {
        return this.session;
    }

    public JsonValue jsonValue() {
        return this.jsonValue;
    }

    /**
     * Navigate a path of keys through nested JSON objects.
     * Returns {@code null} if any key is absent (rather than throwing).
     */
    protected JsonValue at(final String... keys) {
        JsonValue current = this.jsonValue;
        for (final String key : keys) {
            if (!(current instanceof JsonObject obj)) {
                return null;
            }
            current = obj.members().get(key);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    /** Navigate path and return the string value, or {@code ""} if absent or JSON null. */
    protected String text(final String... keys) {
        final JsonValue v = at(keys);
        return switch (v) {
            case JsonString s -> s.value();
            case JsonNumber n -> n.toNumber().toString();
            case JsonNull ignored -> "";
            case null -> "";
            default -> v.toJsonString();
        };
    }

    /** Navigate path and return the int value, or {@code defaultValue} if absent. */
    protected int intAt(final int defaultValue, final String... keys) {
        final JsonValue v = at(keys);
        return v instanceof JsonNumber n ? n.toNumber().intValue() : defaultValue;
    }

    /** Navigate path and return the long value, or {@code defaultValue} if absent. */
    protected long longAt(final long defaultValue, final String... keys) {
        final JsonValue v = at(keys);
        return v instanceof JsonNumber n ? n.toNumber().longValue() : defaultValue;
    }

    /** Navigate path and return the boolean value, or {@code false} if absent. */
    protected boolean boolAt(final String... keys) {
        final JsonValue v = at(keys);
        return v instanceof JsonBoolean b && b.value();
    }

    @Override
    public String toString() {
        return this.jsonValue.toJsonString();
    }
}
