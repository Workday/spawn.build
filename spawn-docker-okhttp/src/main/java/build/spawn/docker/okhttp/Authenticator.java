package build.spawn.docker.okhttp;

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

import okhttp3.Request;

import java.util.function.Function;

/**
 * A {@link Function} to configure authentication for a {@link Request.Builder}.
 *
 * @author brian.oliver
 * @since Aug-2022
 */
public interface Authenticator
    extends Function<Request.Builder, Request.Builder> {

    /**
     * An {@link Authenticator} that performs no authentication.
     */
    Authenticator NONE = builder -> builder;
}
