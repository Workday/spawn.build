package build.spawn.docker.jdk.model;

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
import build.codemodel.injection.Context;
import build.spawn.docker.Container;
import build.spawn.docker.Image;
import build.spawn.docker.jdk.command.CreateContainer;
import build.spawn.docker.jdk.command.DeleteImage;
import build.spawn.docker.jdk.command.InspectImage;
import build.spawn.docker.jdk.command.StartContainer;
import build.spawn.docker.option.ImageName;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.inject.Inject;

import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An internal implementation of an {@link Image}.
 *
 * @author brian.oliver
 * @since Jun-2021
 */
public class DockerImage
    implements Image {

    /**
     * The {@link Context} to create the {@link Image}.
     */
    @Inject
    private Context context;

    /**
     * The identity of the {@link Image}.
     */
    private final String id;

    /**
     * Constructs an {@link DockerImage} with the specified identity.
     *
     * @param id the {@link Image} identity
     */
    public DockerImage(final String id) {
        this.id = Objects.requireNonNull(id, "The Image identity must not be null");
    }

    /**
     * Create a {@link Context} that can be used by the {@link Image} to create
     * {@link build.spawn.docker.jdk.command.Command}s.
     *
     * @return the new {@link Context}
     */
    protected Context createContext() {
        final Context context = this.context.newContext();
        context.bind(build.spawn.docker.Image.class).to(this);
        return context;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public Container start(final Configuration configuration) {

        final Context context = createContext();

        // attempt to create the Container
        final Container container = context
            .inject(new CreateContainer(this, configuration))
            .submit();

        // now start the Container
        try {
            return context
                .inject(new StartContainer(container))
                .submit();
        } catch (final Exception e) {
            container.remove();
            throw e;
        }
    }

    @Override
    public void remove(final Configuration configuration) {
        createContext()
            .inject(new DeleteImage(configuration))
            .submit();
    }

    @Override
    public Stream<ImageName> names() {
        return createContext()
            .inject(new InspectImage(id()))
            .submit()
            .map(ImageInformation.class::cast)
            .map(info ->
                StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(
                            info.jsonNode()
                                .get("RepoTags")
                                .iterator(),
                            Spliterator.ORDERED),
                        false)
                    .map(JsonNode::asText)
                    .map(ImageName::of))
            .orElse(Stream.empty());
    }

    @Override
    public Optional<Information> inspect() {
        return createContext()
            .inject(new InspectImage(id()))
            .submit();
    }
}
