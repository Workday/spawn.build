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

import build.base.foundation.AtomicEnum;
import build.base.foundation.Lazy;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.Closeable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * An abstract {@link Command} that continues to process the {@link Response} after execution, until the
 * {@link Command} is closed or a processing failure occurs.
 *
 * @param <T> the type of result produced by the {@link Command}
 * @author brian.oliver
 * @since Nov-2025
 */
public abstract class AbstractNonBlockingCommand<T> extends AbstractCommand<T>
    implements Closeable {

    /**
     * The {@link Lazy} {@link Call} for the {@link Command}.
     */
    protected final Lazy<Call> call;

    /**
     * The {@link Lazy} {@link Response} to process.
     */
    protected final Lazy<Response> response;

    /**
     * The {@link Lazy} result of the {@link Command}.
     */
    protected final Lazy<T> result;

    /**
     * A {@link CompletableFuture} indicating when the {@link Response} processing has completed, either exceptionally
     * or otherwise.
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
        this.call = Lazy.empty();
        this.response = Lazy.empty();
        this.result = Lazy.empty();
        this.processing = new CompletableFuture<>();
        this.state = AtomicEnum.of(State.INITIALIZING);
    }

    @Override
    protected OkHttpClient httpClient() {
        // ensure the reading from the response never times out (so we can continuously wait for responses)
        return super.httpClient().newBuilder()
            .connectTimeout(0, TimeUnit.MILLISECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .writeTimeout(0, TimeUnit.MILLISECONDS)
            .build();
    }

    @Override
    protected void onRequestCreated(final Request request) {
        // by default, we don't do anything when the Request is created
    }

    @Override
    protected void onCallCreated(final Call call) {
        this.call.set(call);
    }

    @Override
    protected void onResponseReceived(final Response response) {
        this.response.set(response);
        this.state.compareAndSet(State.INITIALIZING, State.PROCESSING);
    }

    @Override
    protected void onSuccessfulRequest(final Request request,
                                       final Response response,
                                       final T result) {

        // capture the result so we can later complete the processing with it
        this.result.set(result);

        // NOTE: we don't close the Response, so that we can continue to consume it
    }

    protected void onProcessingError(final Throwable throwable) {
        if (this.state.compareAndSet(State.PROCESSING, State.COMPLETED)) {
            // complete the processing exceptionally
            this.processing.completeExceptionally(throwable);

            // cancel the Call
            this.call.ifPresent(Call::cancel);
        }
    }

    protected void onProcessingCompletion() {
        if (this.state.compareAndSet(State.PROCESSING, State.COMPLETED)) {
            // complete the processing with the result
            this.processing.complete(this.result.orElse(null));

            // cancel the Call
            this.call.ifPresent(Call::cancel);
        }
    }

    /**
     * Obtains a {@link CompletableFuture} that will be completed when the {@link Response} processing has completed,
     * either exceptionally or otherwise.
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
            // notify that the Command is being closed
            onClosing();

            // complete the processing with the result
            this.processing.complete(this.result.orElse(null));

            // cancel the Call
            this.call.ifPresent(Call::cancel);
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
