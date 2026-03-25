package build.spawn.docker;

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

import build.base.archiving.AbstractTarBuilder;

import java.io.IOException;

/**
 * An {@link build.base.archiving.ArchiveBuilder} that can build a {@code Docker Context} (in a tarball) from which to
 * build {@link Image}s.
 *
 * @author brian.oliver
 * @since Jul-2021
 */
public class DockerContextBuilder
    extends AbstractTarBuilder<DockerContextBuilder> {

    /**
     * Includes the specified {@code Dockerfile} in the {@code Docker Context}.
     *
     * @param builder the {@link DockerFileBuilder}
     * @return this {@link DockerContextBuilder} to permit fluent-style method invocation
     * @throws IOException should it not be possible to build the {@code Dockerfile}
     */
    public DockerContextBuilder withDockerFile(final DockerFileBuilder builder)
        throws IOException {

        in("/").add(builder.build());

        return this;
    }

    // TODO: introduce support for gzipping the built tar file
}
