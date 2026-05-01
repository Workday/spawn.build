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

/**
 * A {@link LauncherRegistration} associates a {@link Launcher} with the {@link AbstractTemplatedPlatform}
 * and {@link Application} type it serves. Implementations are discovered via {@link java.util.ServiceLoader}.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
public interface LauncherRegistration {

    /**
     * The concrete {@link AbstractTemplatedPlatform} class this registration applies to.
     *
     * @return the platform class
     */
    Class<? extends AbstractTemplatedPlatform> platformClass();

    /**
     * The {@link Application} class this {@link Launcher} handles.
     *
     * @return the application class
     */
    Class<? extends Application> applicationClass();

    /**
     * The {@link Launcher} class to use for launching the {@link Application}.
     *
     * @return the launcher class
     */
    Class<? extends Launcher<?, ?>> launcherClass();
}
