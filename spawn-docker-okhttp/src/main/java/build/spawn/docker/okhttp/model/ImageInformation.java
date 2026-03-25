package build.spawn.docker.okhttp.model;

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

import build.spawn.docker.Image;
import build.spawn.docker.option.ExposedPort;

import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An internal implementation of {@link Image.Information}.
 *
 * @author brian.oliver
 * @since Aug-2021
 */
public class ImageInformation
    extends AbstractJsonBasedResult
    implements Image.Information {

    @Override
    public String imageId() {
        return jsonNode().get("Id").asText();
    }

    @Override
    public Stream<ExposedPort> exposedPorts() {
        return StreamSupport
            .stream(
                Spliterators.spliteratorUnknownSize(
                    jsonNode().at("/Config/ExposedPorts").fieldNames(),
                    Spliterator.IMMUTABLE),
                false)
            .map(ExposedPort::of)
            .filter(Optional::isPresent)
            .map(Optional::get);
    }
}
