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
import build.base.foundation.Strings;
import build.base.option.Email;
import build.base.option.Password;
import build.base.option.Username;
import build.spawn.docker.Session;
import build.spawn.docker.option.DockerRegistry;
import build.spawn.docker.option.IdentityToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

/**
 * The {@code Docker Daemon} {@link Command} to authenticate and validate credentials with a Docker Registry using the
 * <a href="https://docs.docker.com/engine/api/v1.41/#tag/System/operation/SystemAuth">Authenticate</a> command.
 *
 * @author brian.oliver
 * @since Aug-2022
 */
public class Authenticate
    extends AbstractBlockingCommand<IdentityToken> {

    /**
     * The {@link ObjectMapper} for parsing json.
     */
    @Inject
    private ObjectMapper objectMapper;

    /**
     * The {@link Username} for authentication.
     */
    @Inject
    private Username username;

    /**
     * The {@link Password} for authentication.
     */
    @Inject
    private Password password;

    /**
     * The {@link DockerRegistry} for authentication.
     */
    @Inject
    private DockerRegistry dockerRegistry;

    /**
     * The {@link Configuration} for the {@link Session}.
     */
    @Inject
    private Configuration configuration;

    @Override
    protected Request createRequest(final HttpUrl.Builder httpUrlBuilder) {

        // establish an ObjectNode containing the auth json
        final var node = this.objectMapper.createObjectNode();

        node.put("username", this.username.get());
        node.put("password", this.password.get());
        this.configuration.getOptionalValue(Email.class)
            .ifPresent(email -> node.put("email", email));
        node.put("serveraddress", this.dockerRegistry.get().toString());

        return new Request.Builder()
            .url(httpUrlBuilder
                .addPathSegment("auth")
                .build())
            .post(RequestBody.create(node.toString(), MEDIA_TYPE_JSON))
            .build();
    }

    private ArrayNode getOrCreateArray(final ObjectNode node, final String key) {
        final var child = node.get(key);
        if (child instanceof ArrayNode) {
            return (ArrayNode) child;
        }
        return node.putArray(key);
    }

    @Override
    protected IdentityToken createResult(final Response response)
        throws IOException {

        final var body = response.body().string();
        final var json = this.objectMapper.readTree(body);

        final var identityToken = json.get("IdentityToken").asText();

        return Strings.isEmpty(identityToken)
            ? IdentityToken.of(this.password.get())
            : IdentityToken.of(identityToken);
    }
}
