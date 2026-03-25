package build.spawn.docker.okhttp.command;

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

import build.base.flow.Publisher;
import build.base.foundation.Lazy;
import build.spawn.docker.Event;
import jakarta.inject.Inject;
import okhttp3.Request;
import okhttp3.Response;

import java.util.concurrent.CompletableFuture;

/**
 * An abstract {@link Command} that blocks until the {@link Command} has been executed and an associated
 * {@link Event} has been received.
 *
 * @param <T> the type of result produced by the {@link Command}
 * @author brian.oliver
 * @since Nov-2025
 */
public abstract class AbstractEventBasedBlockingCommand<T> extends AbstractBlockingCommand<T> {

    /**
     * The {@link Publisher} of {@link Event}s for which we can subscribe.
     */
    @Inject
    private Publisher<Event> publisher;

    /**
     * The {@link CompletableFuture} indicating when the required {@link Event}s have been received or has failed
     */
    private final Lazy<CompletableFuture<?>> processing;

    /**
     * Constructs an {@link AbstractEventBasedBlockingCommand}.
     */
    protected AbstractEventBasedBlockingCommand() {
        this.processing = Lazy.empty();
    }

    /**
     * Subscribes to the specified {@link Publisher} for one or more {@link Event}s.
     *
     * @param publisher the {@link Publisher}
     * @return a {@link CompletableFuture} that will be completed when the {@link Event}s have been received
     */
    abstract protected CompletableFuture<?> subscribe(Publisher<Event> publisher);


    @Override
    protected void onRequestCreated(final Request request) {
        // subscribe to the publisher for the required Events
        this.processing.set(subscribe(this.publisher));

        super.onRequestCreated(request);
    }

    @Override
    protected void onSuccessfulRequest(final Request request,
                                       final Response response,
                                       final T result) {

        super.onSuccessfulRequest(request, response, result);

        // now wait for the required processing to complete
        this.processing.orElseThrow()
            .join();
    }
}
