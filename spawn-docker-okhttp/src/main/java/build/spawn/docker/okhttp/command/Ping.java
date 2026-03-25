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

import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

/**
 * The {@code Docker Daemon} {@link Command} to execute a
 * <a href="https://docs.docker.com/engine/api/v1.41/#operation/SystemPing">System Ping</a>.
 *
 * @author brian.oliver
 * @since Jun-2021
 */
public class Ping
    extends AbstractBlockingCommand<Void> {

    @Override
    protected Request createRequest(final HttpUrl.Builder httpUrlBuilder) {

        return new Request.Builder()
            .url(httpUrlBuilder
                .addPathSegment("_ping")
                .build())
            .build();
    }

    @Override
    protected Void createResult(final Response response) {
        return null;
    }
}
