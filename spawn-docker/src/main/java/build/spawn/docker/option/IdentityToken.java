package build.spawn.docker.option;

/*-
 * #%L
 * Spawn Docker (Client)
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

import build.base.configuration.AbstractValueOption;

import java.util.Objects;

/**
 * An {@link build.base.configuration.Option} representing an <i>Identity Token</i> for a Docker Registry.
 *
 * @author brian.oliver
 * @since Aug-2022
 */
public class IdentityToken
    extends AbstractValueOption<String> {

    /**
     * An empty {@link IdentityToken}.
     */
    private static final IdentityToken EMPTY = new IdentityToken("");

    /**
     * Constructs an {@link IdentityToken}.
     *
     * @param token the non-{@code null} value
     */
    protected IdentityToken(final String token) {
        super(token);
    }

    /**
     * Determines if the {@link IdentityToken} is empty.
     *
     * @return {@code true} when the {@link IdentityToken} is empty, {@code false} otherwise
     */
    public boolean isEmpty() {
        return get().isEmpty();
    }

    /**
     * Attempts to obtain an {@link IdentityToken} given a token {@link String}.
     *
     * @param token the token {@link String}
     * @return an {@link IdentityToken}
     */
    public static IdentityToken of(final String token) {
        Objects.requireNonNull(token, "The token must not be null");
        return new IdentityToken(token.trim());
    }

    /**
     * Obtains an empty {@link IdentityToken}.
     *
     * @return an empty {@link IdentityToken}
     */
    public static IdentityToken empty() {
        return EMPTY;
    }
}
