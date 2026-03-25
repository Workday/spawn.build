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
import build.base.configuration.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * An {@link build.base.configuration.Option} representing the name and optional tag for an
 * {@link build.spawn.docker.Image}.
 *
 * @author brian.oliver
 * @since Jan-2021
 */
public class ImageName
    extends AbstractValueOption<String> {

    /**
     * Constructs a {@link ImageName}.
     *
     * @param name the name of the {@link ImageName}
     */
    private ImageName(final String name) {
        super(Objects.requireNonNull(name, "The Image name must not be null").trim());
    }

    /**
     * Obtains the name of the {@link ImageName}, excluding the optionally defined tag.
     *
     * @return the name of the {@link ImageName}, excluding the optionally defined tag
     */
    public String name() {
        final int index = get().indexOf(':');

        return index < 0
            ? get()
            : get().substring(0, index);
    }

    /**
     * Obtains the {@link Optional} tag for the {@link ImageName}.
     *
     * @return the {@link Optional} tag for the {@link ImageName}
     */
    public Optional<String> tag() {
        final int index = get().indexOf(':');

        return index > 0
            ? Optional.of(get().substring(index + 1))
            : Optional.empty();
    }

    /**
     * Obtains the {@link ImageName} with the tag {@code :latest}.
     *
     * @return the {@code :latest} {@link ImageName}
     */
    public ImageName latest() {
        return get().endsWith(":latest")
            ? this
            : ImageName.of(name() + ":latest");
    }

    /**
     * Constructs an {@link ImageName} given the name and optional tag of an {@link build.spawn.docker.Image}.
     *
     * @param name the name
     * @return a new {@link ImageName}
     */
    public static ImageName of(final String name) {
        return new ImageName(name);
    }

    /**
     * When the user passes in a {@link DockerRegistry} as an option, the image name has to be prefixed with the
     * hostname of the docker registry. Also, if the image name contains a "/", that should not be encoded. This method
     * returns the names that should be added to the path for sending to the {@code REST} server.
     *
     * @param imageName     the name of the image
     * @param configuration the {@link Configuration}
     * @return a {@link List} of strings containing the name
     */
    public static List<String> namesWithDockerRegistry(final String imageName,
                                                       final Configuration configuration) {

        final var names = new ArrayList<String>();
        final var splitNames = imageName.split("/");

        // only include the DockerRegistry when the image name is not a sha256 hash
        // (which should be local images)
        if (!imageName.startsWith("sha256:")) {
            configuration.ifPresent(DockerRegistry.class,
                dockerRegistry -> names.add(dockerRegistry.get().getHost()));
        }

        Collections.addAll(names, splitNames);
        return names;
    }
}

