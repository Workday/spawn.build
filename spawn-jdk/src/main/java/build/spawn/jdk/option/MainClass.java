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

import java.util.Objects;

/**
 * An {@link Option} to define the main-class of a {@link JDKApplication}.
 *
 * @author brian.oliver
 * @since Oct-2018
 */
public class MainClass
    extends AbstractValueOption<String> {

    /**
     * Constructs a {@link MainClass}.
     *
     * @param className the {@link Class} name of the main-class
     */
    private MainClass(final String className) {
        super(className);
    }

    /**
     * Obtains the {@link Class} name of the {@link MainClass}.
     *
     * @return the {@link Class} name
     */
    public String className() {
        return get();
    }

    /**
     * Creates a {@link MainClass} from the specified main-class name.
     *
     * @param className the {@link Class} name of the main-class
     * @return a {@link MainClass}
     */
    public static MainClass of(final String className) {
        Objects.requireNonNull(className, "The main class name must not be null");
        return new MainClass(className);
    }

    /**
     * Creates a {@link MainClass} from the specified {@link Class}.
     *
     * @param mainClass the {@link Class} containing the main-method
     * @return a {@link MainClass}
     */
    public static MainClass of(final Class<?> mainClass) {
        Objects.requireNonNull(mainClass, "The main class must not be null");

        // TODO: validate the class contains a public static void main method with String[] arguments
        return new MainClass(mainClass.getName());
    }
}
