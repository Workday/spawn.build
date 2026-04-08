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

import build.base.configuration.Configuration;
import build.base.option.Timeout;
import build.spawn.docker.Container;
import build.spawn.docker.jdk.HttpTransport;
import jakarta.inject.Inject;

import java.io.IOException;

/**
 * The {@code Docker Engine} {@link Command} to stop a {@code Container} using the
 * <a href="https://docs.docker.com/engine/api/v1.41/#operation/ContainerStop">Stop Container</a> command.
 *
 * @author brian.oliver
 * @since Jun-2021
 */
public class StopContainer
    extends AbstractBlockingCommand<Container> {

    /**
     * The {@link Container} to stop.
     */
    @Inject
    private Container container;

    /**
     * The {@link Configuration} for the {@link Command}.
     */
    private final Configuration configuration;

    /**
     * Constructs a {@link StopContainer} {@link Command}.
     *
     * @param configuration the {@link Configuration} to stop the {@link Container}
     */
    public StopContainer(final Configuration configuration) {
        this.configuration = configuration == null
            ? Configuration.empty()
            : configuration;
    }

    @Override
    protected HttpTransport.Request createRequest() {

        final Timeout timeout = this.configuration.get(Timeout.class);
        return HttpTransport.Request.post(
            "/containers/" + this.container.id() + "/stop?t=" + timeout.get().getSeconds(), null);
    }

    @Override
    protected void onUnsuccessfulRequest(final HttpTransport.Request request, final HttpTransport.Response response)
        throws IOException {

        // a response of 304 or 404 means the Container is already stopped or doesn't exist
        if (response.statusCode() != 304 && response.statusCode() != 404) {
            super.onUnsuccessfulRequest(request, response);
        }
    }

    @Override
    protected Container createResult(final HttpTransport.Response response) {
        return this.container;
    }
}
