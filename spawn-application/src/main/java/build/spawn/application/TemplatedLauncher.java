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

import build.base.configuration.ConfigurationBuilder;
import build.spawn.application.option.Executable;
import build.spawn.application.option.Name;

import java.util.Optional;

/**
 * A {@link Launcher} using the
 * <a href="https://en.wikipedia.org/wiki/Template_method_pattern">template pattern</a> providing skeleton
 * implementations for launching applications on a variety of {@link Platform}s.
 *
 * @param <A> the type of {@link Application} that will be launched
 * @param <P> the type of {@link Platform} on which the {@link Application} will be launched
 * @param <N> the type of {@link Process} representing managing the {@link Application} when it's launched
 * @author brian.oliver
 * @since Nov-2024
 */
public interface TemplatedLauncher<A extends Application, P extends Platform, N extends Process>
    extends Launcher<A, P> {

    /**
     * Obtains the {@link Optional} {@link Executable} to launch for an {@link Application}.
     * <p>
     * Some {@link Application}s can be launched without an {@link Executable}, for example, those that are based
     * on a {@code Docker Image}.  Some may be launched without an {@link Executable} as a suitable default may be used.
     * However, others will always require an {@link Executable}.  Should a {@link TemplatedLauncher}
     * require an {@link Executable}, but one isn't present or can't be determined, an
     * {@link IllegalArgumentException} may be thrown by the {@link TemplatedLauncher} when launching.
     *
     * @param options the {@link ConfigurationBuilder} used to determine the default {@link Executable}
     * @return an {@link Optional} {@link Executable}
     */
    default Optional<Executable> getExecutable(final ConfigurationBuilder options) {
        return Optional.ofNullable(options.get(Executable.class));
    }

    /**
     * Obtain the {@link Name} of the {@link Application}.
     * <p>
     * Should a {@link Name} not be available or discoverable, a unique name will be generated.
     *
     * @param options the {@link ConfigurationBuilder} used to determine the default {@link Name}
     * @return a suitable default {@link Name}
     */
    Name getName(ConfigurationBuilder options);

    /**
     * Creates an {@link Process} for the {@link Application}, redirecting I/O as appropriate.
     *
     * @param platform the {@link Platform} upon which the {@link Process} is to be launched
     * @param options  the {@link ConfigurationBuilder} customizing and launching the {@link Process}
     * @return a {@link Process} running the {@link Application}
     */
    N createProcess(P platform, ConfigurationBuilder options);
}
