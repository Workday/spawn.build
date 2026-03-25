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
import build.base.configuration.Option;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Objects;

/**
 * An {@link Option} defining the {@link URL} of a Docker Registry.
 *
 * @author brian.oliver
 * @since Aug-2022
 */
public class DockerRegistry
    extends AbstractValueOption<URL> {

    /**
     * Constructs a {@link DockerRegistry}.
     *
     * @param url the non-{@code null} value
     */
    protected DockerRegistry(final URL url) {
        super(url);
    }

    /**
     * Attempts to obtain a {@link DockerRegistry} for the provided url {@link String}.
     *
     * @param url the {@link URL} {@link String}
     * @return a {@link DockerRegistry}
     * @throws MalformedURLException when the provided {@link String} is not a valid {@link URL}
     */
    public static DockerRegistry of(final String url)
        throws MalformedURLException {

        Objects.requireNonNull(url, "The URL must not be null");
        return new DockerRegistry(URI.create(url).toURL());
    }
}
