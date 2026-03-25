package build.spawn.docker.okhttp.option;

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

import build.base.configuration.AbstractValueOption;

import java.time.Duration;

/**
 * A {@link ConnectTimeout} for OkHttp.
 *
 * @author anand.sankaran
 * @since Aug-2022
 */
public class ConnectTimeout
    extends AbstractValueOption<Duration> {

    /**
     * Constructs an {@link ConnectTimeout}.
     *
     * @param value the non-{@code null} value
     */
    public ConnectTimeout(final Duration value) {
        super(value);
    }
}
