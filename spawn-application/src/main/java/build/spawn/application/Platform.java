package build.spawn.application;

/*-
 * #%L
 * Spawn Application
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
import build.base.configuration.ConfigurationBuilder;
import build.base.configuration.Option;
import build.spawn.application.option.Executable;

/**
 * A facility to launch and manage the lifecycle of {@link Application}s at runtime.
 *
 * @author Brian Oliver
 * @since Jun-2020
 */
public interface Platform {

    /**
     * Obtains the name of the {@link Platform}.
     *
     * @return the name of the {@link Platform}
     */
    String name();

    /**
     * Obtains the {@link Configuration} used to establish the {@link Platform}, from which default {@link Option}s
     * will be drawn and used to launch {@link Application}s.
     *
     * @return the {@link Configuration} for the {@link Platform}
     */
    Configuration configuration();

    /**
     * Launches an {@link Application} using the specified {@link Specification}.
     *
     * @param specification the {@link Specification}
     * @param <A>           the type of {@link Application}
     * @return the newly launched {@link Application}
     */
    <A extends Application> A launch(Specification<A> specification);

    /**
     * Launches a particular {@link Class} of {@link Application} with the provided {@link Option}s.
     *
     * @param <A>                  the type of {@link Application}
     * @param applicationClass     the {@link Class} of {@link Application}
     * @param configurationBuilder the {@link ConfigurationBuilder} of {@link Option}s for the {@link Application}
     * @return the newly launched {@link Application}
     */
    default <A extends Application> A launch(final Class<A> applicationClass,
                                             final ConfigurationBuilder configurationBuilder) {

        return launch(Specification.of(applicationClass, configurationBuilder));
    }

    /**
     * Launches a particular {@link Class} of {@link Application} with the provided {@link Option}s.
     *
     * @param <A>              the type of {@link Application}
     * @param applicationClass the {@link Class} of {@link Application}
     * @param options          the {@link Option}s for launching the {@link Application}
     * @return the newly launched {@link Application}
     */
    default <A extends Application> A launch(final Class<A> applicationClass, final Option... options) {
        return launch(Specification.of(applicationClass, options));
    }

    /**
     * Launches an {@link Application} using the specified executable and provided launch {@link Option}s.
     *
     * @param executable the executable
     * @param options    the {@link Option}s for launching the {@link Application}
     * @return the newly launched {@link Application}
     */
    default Application launch(final String executable, final Option... options) {

        // include the specified executable as a launch option
        final var configurationBuilder = ConfigurationBuilder
            .create(options)
            .add(Executable.of(executable));

        return launch(Specification.of(Application.class, configurationBuilder));
    }
}
