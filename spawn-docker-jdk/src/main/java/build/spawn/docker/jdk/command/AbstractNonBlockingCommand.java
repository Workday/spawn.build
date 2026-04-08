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

import build.base.foundation.AtomicEnum;
import build.base.foundation.Lazy;
import build.spawn.docker.jdk.HttpTransport;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

/**
 * An abstract {@link Command} that continues to process the response after execution, until the
 * {@link Command} is closed or a processing failure occurs.
 *
 * @param <T> the type of result produced by the {@link Command}
 * @author brian.oliver
 * @since Nov-2025
 */
public abstract class AbstractNonBlockingCommand<T> extends AbstractCommand<T>
    implements Closeable {

    /**
     * The active {@link HttpTransport.Response} being processed.
     */
    protected final Lazy<HttpTransport.Response> activeResponse;

    /**
     * The {@link Lazy} result of the {@link Command}.
     */
    protected final Lazy<T> result;

    /**
     * A {@link CompletableFuture} indicating when processing has completed.
     */
    private final CompletableFuture<T> processing;

    /**
     * An internal {@link State} for the potentially long-running {@link Command}.
     */
    private final AtomicEnum<State> state;

    /**
     * Constructs an {@link AbstractNonBlockingCommand}.
     */
    protected AbstractNonBlockingCommand() {
        this.activeResponse = Lazy.empty();
        this.result = Lazy.empty();
        this.processing = new CompletableFuture<>();
        this.state = AtomicEnum.of(State.INITIALIZING);
    }

    @Override
    protected void onRequestCreated(final HttpTransport.Request request) {
        // by default, we don't do anything when the Request is created
    }

    @Override
    protected void onResponseReceived(final HttpTransport.Response response) {
        this.activeResponse.set(response);
        this.state.compareAndSet(State.INITIALIZING, State.PROCESSING);
    }

    @Override
    protected void onSuccessfulRequest(final HttpTransport.Request request,
                                       final HttpTransport.Response response,
                                       final T result) {
        this.result.set(result);
        // NOTE: we don't close the Response, so that we can continue to consume it
    }

    protected void onProcessingError(final Throwable throwable) {
        if (this.state.compareAndSet(State.PROCESSING, State.COMPLETED)) {
            this.processing.completeExceptionally(throwable);
            this.activeResponse.ifPresent(HttpTransport.Response::cancel);
        }
    }

    protected void onProcessingCompletion() {
        if (this.state.compareAndSet(State.PROCESSING, State.COMPLETED)) {
            this.processing.complete(this.result.orElse(null));
            this.activeResponse.ifPresent(HttpTransport.Response::cancel);
        }
    }

    /**
     * Obtains a {@link CompletableFuture} that will be completed when processing has completed.
     *
     * @return the {@link CompletableFuture}
     */
    public CompletableFuture<T> processing() {
        return this.processing;
    }

    /**
     * Handle when the {@link Command} is being closed.
     */
    protected void onClosing() {
        // by default, we don't do anything when the Command is being closed
    }

    @Override
    public void close() {
        if (this.state.compareAndSet(State.PROCESSING, State.COMPLETED)) {
            onClosing();
            this.processing.complete(this.result.orElse(null));
            this.activeResponse.ifPresent(HttpTransport.Response::cancel);
        }
    }

    /**
     * An internal {@link State} for the potentially long-running {@link Command}.
     */
    private enum State {
        INITIALIZING,
        PROCESSING,
        COMPLETED
    }
}
