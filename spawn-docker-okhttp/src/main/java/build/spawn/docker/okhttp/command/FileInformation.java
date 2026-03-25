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

import build.spawn.docker.Container;
import jakarta.inject.Inject;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A command to retrieve information about a file from a running container.
 *
 * @author anand.sankaran
 * @since Aug-2022
 */
public class FileInformation
    extends AbstractBlockingCommand<Optional<Map<String, String>>> {

    /**
     * The {@link Path} to get information.
     */
    private final Path filePath;

    /**
     * The current container.
     */
    @Inject
    private Container container;

    /**
     * Constructor.
     *
     * @param filePath the {@link Path} to get information
     */
    public FileInformation(final Path filePath) {
        this.filePath = filePath;
    }

    @Override
    protected Request createRequest(final HttpUrl.Builder httpUrlBuilder) {
        return new Request.Builder()
            .url(httpUrlBuilder
                .addPathSegment("containers")
                .addPathSegment(this.container.id())
                .addPathSegment("archive")
                .addQueryParameter("path", this.filePath.toFile().getAbsolutePath())
                .build())
            .head()
            .build();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Optional<Map<String, String>> createResult(final Response response)
        throws IOException {
        if (response.code() != 200) {
            return Optional.empty();
        }
        final String base64Encoded = response.header("x-docker-container-path-stat");
        final byte[] decoded = Base64.getDecoder().decode(base64Encoded);
        final String json = new String(decoded, StandardCharsets.UTF_8);
        final ObjectMapper mapper = new ObjectMapper();
        final Map<String, String> map = mapper.readValue(json, Map.class);
        return Optional.of(map);
    }
}
