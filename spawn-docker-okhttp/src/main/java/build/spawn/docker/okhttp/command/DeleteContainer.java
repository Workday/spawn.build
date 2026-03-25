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
import build.spawn.docker.option.RemoveVolumes;
import jakarta.inject.Inject;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * The {@code Docker Engine} {@link Command} to
 * <a href="https://docs.docker.com/engine/api/v1.41/#operation/ContainerDelete">Delete</a> a {@link Container}.
 *
 * @author brian.oliver
 * @since Jun-2021
 */

public class DeleteContainer
    extends AbstractBlockingCommand<Void> {

    /**
     * The {@link Container} to delete.
     */
    @Inject
    private Container container;

    /**
     * The {@link Configuration} on how to delete the {@link Container}.
     */
    private final Configuration configuration;

    /**
     * Constructs a {@link DeleteContainer} {@link Command}.
     *
     * @param configuration the {@link Container} to use for deleting the container
     */
    public DeleteContainer(final Configuration configuration) {
        this.configuration = configuration == null
            ? Configuration.empty()
            : configuration;
    }

    @Override
    protected Request createRequest(final HttpUrl.Builder httpUrlBuilder) {
        return new Request.Builder()
            .url(httpUrlBuilder
                .addPathSegment("containers")
                .addPathSegment(this.container.id())
                .addQueryParameter("v", String.valueOf(this.configuration.getValue(RemoveVolumes.class)))
                .addQueryParameter("force", "true")
                .build())
            .delete()
            .build();
    }

    @Override
    protected Void createResult(final Response response)
        throws IOException {

        return null;
    }
}
