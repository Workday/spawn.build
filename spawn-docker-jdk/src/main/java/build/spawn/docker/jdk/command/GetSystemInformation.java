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

import build.spawn.docker.Session;
import build.spawn.docker.jdk.HttpTransport;
import build.spawn.docker.jdk.model.AbstractJsonBasedResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;

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

    /**
     * The {@link ObjectMapper} for parsing json.
     */
    @Inject
    private ObjectMapper objectMapper;

    @Override
    protected HttpTransport.Request createRequest() {
        return HttpTransport.Request.get("/info");
    }

    @Override
    protected Session.Information createResult(final HttpTransport.Response response)
        throws IOException {

        // establish a new Context to create the Result
        final var context = createContext();

        // bind the JsonNode representation of the response
        final var json = response.bodyString();

        final var jsonNode = this.objectMapper.readTree(json);
        context.bind(JsonNode.class).to(jsonNode);

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
            return jsonNode().get("ServerVersion").asText();
        }
    }
}
