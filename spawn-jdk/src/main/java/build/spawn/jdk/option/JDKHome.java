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
import build.base.configuration.Default;
import build.base.configuration.Option;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * An {@link Option} to define the JDK Home directory, the default being based on the {@code java.home} system property.
 *
 * @author graeme.campbell
 * @since Dec-2018
 */
public class JDKHome
    extends AbstractValueOption<String> {

    /**
     * Constructs a {@link JDKHome}.
     *
     * @param home the java home
     */
    private JDKHome(final String home) {
        super(home);
    }

    /**
     * A {@link Path} representation of the java home.
     *
     * @return a {@link Path}
     */
    public Path path() {
        return Paths.get(get());
    }

    /**
     * Creates a {@link JDKHome}.
     *
     * @param home the java home
     * @return a {@link JDKHome}
     */
    public static JDKHome of(final String home) {
        return new JDKHome(home);
    }

    /**
     * Obtains the current {@link JDKHome} for the JVM, which is defined by the system property
     * {@code java.home}.
     *
     * @return a {@link JDKHome}
     */
    @Default
    @SuppressWarnings("unused")
    public static JDKHome current() {
        return JDKHome.of(System.getProperty("java.home"));
    }
}
