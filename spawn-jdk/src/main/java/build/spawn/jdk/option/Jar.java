package build.spawn.jdk.option;

/*-
 * #%L
 * Spawn JDK
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
import build.spawn.jdk.JDKApplication;

import java.nio.file.Path;
import java.util.Objects;

/**
 * An {@link Option} to define the {@link Path} of jar to launch for a {@link JDKApplication}, thus using the
 * java command line option <code>-jar some.jar</code> instead of specifying a main class.
 *
 * @author brian.oliver
 * @since Oct-2019
 */
public class Jar
    extends AbstractValueOption<Path> {

    /**
     * Constructs a {@link Jar}.
     *
     * @param path the {@link Path} to the jar to execute
     */
    private Jar(final Path path) {
        super(path);
    }

    /**
     * Creates a {@link Jar} from the specified jar {@link Path}.
     *
     * @param path the {@link Path} of the jar to execute
     * @return a {@link Jar}
     */
    public static Jar of(final Path path) {
        Objects.requireNonNull(path, "The jar path must not be null");

        return new Jar(path);
    }
}
