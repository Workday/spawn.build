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

import build.spawn.docker.jdk.HttpTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;

import java.io.IOException;

/**
 * A command to delete networks.
 *
 * @author anand.sankaran
 * @since Aug-2022
 */
public class DeleteNetwork
    extends AbstractBlockingCommand<Boolean> {

    /**
     * The name of the network.
     */
    private final String nameOrId;

    /**
     * The {@link ObjectMapper} for parsing json.
     */
    @Inject
    private ObjectMapper objectMapper;

    /**
     * Constructor.
     *
     * @param nameOrId name of the network to delete.
     */
    public DeleteNetwork(final String nameOrId) {
        this.nameOrId = nameOrId;
    }

    @Override
    protected HttpTransport.Request createRequest() {
        return HttpTransport.Request.delete("/networks/" + this.nameOrId);
    }

    @Override
    protected Boolean createResult(final HttpTransport.Response response)
        throws IOException {
        return response.statusCode() == 204;
    }
}
