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

/**
 * A mechanism to launch an instance of a specific {@link Class} of {@link Application} on a {@link Platform}.
 * <p>
 * {@link Launcher}s perform the <i>actual work</i> of launching an {@link Application} on behalf of a {@link Platform}.
 * <p>
 * To launch an {@link Application}, a {@link Platform} first establishes a new {@link Configuration} to capture the
 * {@link Option}s required to launch the {@link Application}, after wards, delegating the actual work of
 * launching of the {@link Application} to a suitable {@link Launcher#launch(Platform, Class, Configuration)}.
 * <p>
 * {@link Launcher}s thus have the ultimate responsibility for any <i>remaining</i> launch configuration, prior to
 * finally launching the {@link Application}.
 * <p>
 * Once an {@link Application} is launched, the associated {@link Launcher} and provided {@link Configuration} is
 * discarded by the {@link Platform}.
 * <p>
 * The final {@link Configuration} used for launching the {@link Application} however is made available via
 * the {@link Application#configuration()} method.
 *
 * @param <A> the type of {@link Application} that will be launched
 * @param <P> the type of {@link Platform} on which the {@link Application} will be launched
 * @author brian.oliver
 * @since Nov-2024
 */
@FunctionalInterface
public interface Launcher<A extends Application, P extends Platform> {

    /**
     * Launches an {@link Application} on the specified {@link Platform} using the provided {@link ConfigurationBuilder}
     * as the basis of initial configuration {@link Option}s.
     *
     * @param platform         the {@link Platform}
     * @param applicationClass the {@link Class} of {@link Application}
     * @param configuration    the {@link Configuration}
     * @return the newly launched {@link Application}
     */
    A launch(P platform,
             Class<? extends A> applicationClass,
             Configuration configuration);
}
