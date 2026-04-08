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
import build.spawn.docker.Image;
import build.spawn.docker.jdk.HttpTransport;
import jakarta.inject.Inject;

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
    protected HttpTransport.Request createRequest() {
        return HttpTransport.Request.delete("/images/" + this.image.id() + "?force=true");
    }

    @Override
    protected void onUnsuccessfulRequest(final HttpTransport.Request request, final HttpTransport.Response response)
        throws IOException {

        // 404 (missing image) is not considered a failure
        if (response.statusCode() != 404) {
            super.onUnsuccessfulRequest(request, response);
        }
    }

    @Override
    protected Void createResult(final HttpTransport.Response response)
        throws IOException {

        return null;
    }
}
