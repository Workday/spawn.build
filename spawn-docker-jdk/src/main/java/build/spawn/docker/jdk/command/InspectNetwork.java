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
import build.spawn.docker.Network;
import build.spawn.docker.jdk.HttpTransport;
import build.spawn.docker.jdk.model.NetworkInformation;

import java.io.IOException;
import java.util.Optional;

/**
 * A command to get network information.
 *
 * @author anand.sankaran
 * @since Aug-2022
 */
public class InspectNetwork
    extends AbstractBlockingCommand<Optional<Network.Information>> {

    private final String nameOrId;

    /**
     * Constructor.
     *
     * @param nameOrId name or id of the network.
     */
    public InspectNetwork(final String nameOrId) {
        this.nameOrId = nameOrId;
    }

    @Override
    protected HttpTransport.Request createRequest() {
        return HttpTransport.Request.get("/networks/" + this.nameOrId);
    }

    @Override
    protected Optional<Network.Information> createResult(final HttpTransport.Response response)
        throws IOException {

        if (response.statusCode() == 404) {
            return Optional.empty();
        }

        // establish a new Context to create the Result
        final var context = createContext();

        // bind the JsonValue representation of the response
        context.bind(JsonValue.class).to(Json.parse(response.bodyString()));

        return Optional.of(context.create(NetworkInformation.class));
    }
}
