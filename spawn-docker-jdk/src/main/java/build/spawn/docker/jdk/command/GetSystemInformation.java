package build.spawn.docker.jdk.command;

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

import build.base.json.Json;
import build.base.json.JsonValue;
import build.spawn.docker.Session;
import build.spawn.docker.jdk.HttpTransport;
import build.spawn.docker.jdk.model.AbstractJsonBasedResult;

import java.io.IOException;

/**
 * The {@code Docker Daemon} {@link Command} to obtain
 * <a href="https://docs.docker.com/engine/api/v1.41/#operation/SystemInfo">System Information</a>.
 *
 * @author brian.oliver
 * @since Jun-2021
 */
public class GetSystemInformation
    extends AbstractBlockingCommand<Session.Information> {

    @Override
    protected HttpTransport.Request createRequest() {
        return HttpTransport.Request.get("/info");
    }

    @Override
    protected Session.Information createResult(final HttpTransport.Response response)
        throws IOException {

        // establish a new Context to create the Result
        final var context = createContext();

        // bind the JsonValue representation of the response
        context.bind(JsonValue.class).to(Json.parse(response.bodyString()));

        return context.create(Result.class);
    }

    /**
     * The {@link Session.Information} result.
     */
    private static class Result
        extends AbstractJsonBasedResult
        implements Session.Information {

        @Override
        public String getServerVersion() {
            return text("ServerVersion");
        }
    }
}
