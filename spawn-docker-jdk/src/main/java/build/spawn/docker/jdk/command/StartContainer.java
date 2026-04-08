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

import build.base.flow.CompletingSubscriber;
import build.spawn.docker.Container;
import build.spawn.docker.Event;
import build.spawn.docker.jdk.HttpTransport;
import jakarta.inject.Inject;

import java.util.Objects;

/**
 * The {@code Docker Daemon} {@link Command} to start a {@code Container} using the
 * <a href="https://docs.docker.com/engine/api/v1.41/#operation/ContainerStart">Start Container</a> command.
 *
 * @author brian.oliver
 * @since Jun-2021
 */
public class StartContainer
    extends AbstractBlockingCommand<Container> {

    /**
     * The {@link Container} to start.
     */
    private final Container container;

    /**
     * The {@link CompletingSubscriber} for {@code Docker Engine} {@link Event}s.
     */
    @Inject
    private CompletingSubscriber<Event> eventSubscriber;

    /**
     * Constructs a {@link StartContainer} {@link Command}.
     *
     * @param container the {@link Container} to start
     */
    public StartContainer(final Container container) {
        this.container = Objects.requireNonNull(container, "The Container must not be null");
    }

    @Override
    protected HttpTransport.Request createRequest() {
        return HttpTransport.Request.post(
            "/containers/" + this.container.id() + "/start", null);
    }

    @Override
    protected Container createResult(final HttpTransport.Response response) {
        return this.container;
    }
}
