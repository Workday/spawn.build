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
import build.codemodel.injection.Context;
import build.codemodel.injection.InjectionFramework;
import build.codemodel.jdk.JDKCodeModel;

/**
 * A {@link Platform} which allows its implementations to specify a {@link Context} with which to instantiate that
 * {@link Platform}'s {@link Launcher}.
 *
 * @author brian.oliver
 * @since Nov-2024
 */
public interface TemplatedPlatform
    extends Platform {

    /**
     * Obtains the {@link InjectionFramework} for use by the {@link TemplatedPlatform}.
     *
     * @return an {@link InjectionFramework}
     */
    InjectionFramework injectionFramework();

    /**
     * Obtains the {@link JDKCodeModel} used by the {@link TemplatedPlatform}.
     *
     * @return the {@link JDKCodeModel}
     */
    default JDKCodeModel codeModel() {
        return injectionFramework().codeModel();
    }

    /**
     * Creates a new {@link Context} for the specified {@link Class} of {@link Application} using the
     * {@link TemplatedPlatform#injectionFramework()} with the specified {@link Configuration}, that a {@link Platform}
     * may then use to launch the specified {@link Class} of{@link Application}.
     * <p>
     * While the returned {@link Context} is typically used for instantiating a). the {@link Launcher} to
     * be used to launch the {@link Class} of {@link Application}, and b). the {@link Application} itself.  The
     * {@link Context} may also be used for instantiating other {@link Class}es or injecting into other {@link Object}s
     * the {@link Platform} or {@link Launcher} requires for the {@link Application}.
     *
     * @param applicationClass the {@link Class} of {@link Application} being launched
     * @param configuration    the {@link Configuration} for launching the {@link Class} of {@link Application}
     * @return a new {@link Context} for launching the specified {@link Class} of {@link Application}
     */
    Context createContext(Class<? extends Application> applicationClass,
                          Configuration configuration);
}
