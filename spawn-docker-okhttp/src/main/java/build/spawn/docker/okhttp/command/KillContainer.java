package build.spawn.docker.okhttp.command;

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

import build.base.configuration.Configuration;
import build.spawn.docker.Container;
import build.spawn.docker.option.KillSignal;
import jakarta.inject.Inject;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * The {@code Docker Engine} {@link Command} to kill a {@code Container} using the
 * <a href="https://docs.docker.com/engine/api/v1.41/#operation/ContainerKill">Kill Container</a> command.
 *
 * @author brian.oliver
 * @since Jun-2021
 */
public class KillContainer
    extends AbstractBlockingCommand<Container> {

    /**
     * The {@link Container} to kill.
     */
    @Inject
    private Container container;

    /**
     * The {@link Configuration} for the {@link Command}.
     */
    private final Configuration configuration;

    /**
     * Constructs a {@link KillContainer} {@link Command}.
     *
     * @param configuration the {@link Configuration} to kill the {@link Container}
     */
    public KillContainer(final Configuration configuration) {
        this.configuration = configuration == null
            ? Configuration.empty()
            : configuration;
    }

    @Override
    protected Request createRequest(final HttpUrl.Builder httpUrlBuilder) {

        final KillSignal signal = this.configuration.get(KillSignal.class);

        return new Request.Builder()
            .url(httpUrlBuilder
                .addPathSegment("containers")
                .addPathSegment(this.container.id())
                .addPathSegment("kill")
                .addQueryParameter("signal", signal.signalName())
                .build())
            .post(EMPTY_BODY)
            .build();
    }

    @Override
    protected void onUnsuccessfulRequest(final Request request, final Response response)
        throws IOException {

        // a response of 204 means success, 404 or 409 means the Container doesn't exist or isn't running
        if (response.code() != 204 && response.code() != 404 && response.code() != 409) {
            super.onUnsuccessfulRequest(request, response);
        }
    }

    @Override
    protected Container createResult(final Response response) {
        return this.container;
    }
}
