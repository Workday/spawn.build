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
 * An abstract {@link Command} that blocks until the {@link Command} has been executed and a response has been
 * completely processed.
 *
 * @param <T> the type of result produced by the {@link Command}
 * @author brian.oliver
 * @since Nov-2025
 */
public abstract class AbstractBlockingCommand<T> extends AbstractCommand<T> {

    @Override
    protected void onRequestCreated(final HttpTransport.Request request) {
        // by default, we don't do anything when the Request is created
    }

    @Override
    protected void onResponseReceived(final HttpTransport.Response response) {
        // by default, we don't do anything with the Response when it is received
    }

    @Override
    protected void onSuccessfulRequest(final HttpTransport.Request request,
                                       final HttpTransport.Response response,
                                       final T result) {
        response.close();
    }
}
