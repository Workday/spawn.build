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
import build.spawn.docker.Image;
import jakarta.inject.Inject;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * The {@code Docker Engine} {@link Command} to
 * <a href="https://docs.docker.com/engine/api/v1.41/#operation/ImageDelete">Delete</a> an {@link Image}.
 *
 * @author brian.oliver
 * @since Jul-2021
 */

public class DeleteImage
    extends AbstractBlockingCommand<Void> {

    /**
     * The {@link Image} to delete.
     */
    @Inject
    private Image image;

    /**
     * Constructs a {@link DeleteImage} {@link Command}.
     *
     * @param configuration the {@link Configuration} to use when attaching
     */
    public DeleteImage(final Configuration configuration) {
    }

    @Override
    protected Request createRequest(final HttpUrl.Builder httpUrlBuilder) {
        return new Request.Builder()
            .url(httpUrlBuilder
                .addPathSegment("images")
                .addPathSegment(this.image.id())
                .addQueryParameter("force", "true")
                .build())
            .delete()
            .build();
    }

    @Override
    protected void onUnsuccessfulRequest(final Request request, final Response response)
        throws IOException {

        // 404 (missing image) is not considered a failure
        if (response.code() != 404) {
            super.onUnsuccessfulRequest(request, response);
        }
    }

    @Override
    protected Void createResult(final Response response)
        throws IOException {

        return null;
    }
}
