package build.spawn.docker.jdk.command;

/*-
 * #%L
 * Spawn Docker (JDK Client)
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

import build.spawn.docker.jdk.HttpTransport;

/**
 * The {@code Docker Daemon} {@link Command} to execute a
 * <a href="https://docs.docker.com/engine/api/v1.41/#operation/SystemPing">System Ping</a>.
 *
 * @author brian.oliver
 * @since Jun-2021
 */
public class Ping
    extends AbstractBlockingCommand<Void> {

    @Override
    protected HttpTransport.Request createRequest() {
        return HttpTransport.Request.get("/_ping");
    }

    @Override
    protected Void createResult(final HttpTransport.Response response) {
        return null;
    }
}
