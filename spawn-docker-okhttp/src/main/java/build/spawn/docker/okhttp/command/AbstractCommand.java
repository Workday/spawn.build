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

import build.codemodel.injection.Context;
import jakarta.inject.Inject;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;

/**
 * An abstract {@link Command} that uses an {@link OkHttpClient} to submit request(s) and receive responses,
 * that of which may be processed to produce a result.
 *
 * @param <T> the type of result produced by the {@link Command}
 * @author brian.oliver
 * @since Jun-2021
 */
public abstract class AbstractCommand<T>
    implements Command<T> {

    /**
     * The {@link MediaType} for JSON.
     */
    protected final static MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

    /**
     * An empty {@link RequestBody}.
     */
    protected final static RequestBody EMPTY_BODY = RequestBody.create("", null);

    /**
     * The {@link OkHttpClient} to use for executing the {@link Command}.
     */
    @Inject
    private OkHttpClient httpClient;

    /**
     * The {@link HttpUrl.Builder} to use for creating the {@link Command}.
     */
    @Inject
    private HttpUrl.Builder httpUrlBuilder;

    /**
     * The dependency injection {@link Context} for the {@link Command}.
     */
    @Inject
    private Context context;

    /**
     * Obtains the {@link OkHttpClient} to use for executing {@link Command}s.
     *
     * @return the {@link OkHttpClient}
     */
    protected OkHttpClient httpClient() {
        return this.httpClient;
    }

    /**
     * Creates a new dependency injection {@link Context}, based on the {@link Context} used to create the
     * {@link Command}.
     *
     * @return a new {@link Context}
     */
    protected Context createContext() {
        return this.context.newContext();
    }

    /**
     * Obtains the {@link Request} to execute for the {@link Command}.
     *
     * @param httpUrlBuilder the {@link HttpUrl.Builder} for constructing the {@link Request} {@link HttpUrl}
     * @return the {@link Request}
     */
    abstract protected Request createRequest(HttpUrl.Builder httpUrlBuilder);

    /**
     * Create the result for the {@link Command} based on the successful {@link Response}.
     *
     * @param response the {@link Response}
     * @return the result
     * @throws IOException should processing the {@link Response} fail
     */
    abstract protected T createResult(Response response)
        throws IOException;

    /**
     * Handle when the {@link Request} has been created for the {@link Command}, but not yet sent for execution.
     *
     * @param request the {@link Request}
     */
    abstract protected void onRequestCreated(Request request);

    /**
     * Handle when the {@link Call} has been created for the {@link Command}, but not yet sent for execution.
     *
     * @param call the {@link Call}
     */
    abstract protected void onCallCreated(Call call);

    /**
     * Handle when the {@link Response} has been received for the {@link Request} representing the {@link Command},
     * prior to it being processed to establish a result.
     *
     * @param response the {@link Response}
     */
    abstract protected void onResponseReceived(Response response);

    /**
     * Handle when the execution of a {@link Request} was successful.
     * <p>
     * By default, this method closes the {@link Response}.
     *
     * @param request  the {@link Request}
     * @param response the {@link Response}
     * @param result   the result
     */
    abstract protected void onSuccessfulRequest(Request request,
                                                Response response,
                                                T result);

    /**
     * Handle when the execution of a {@link Request} was unsuccessful as indicated by {@link Response#isSuccessful()}
     * returning {@code false}.
     * <p>
     * By default, this method throws a {@link IOException} containing the {@link Response#code()} for the
     * {@link Request}.  Should this method return without throwing an {@link IOException}, an attempt while be made
     * to process and create the result for the {@link Command} using {@link #createResult(Response)}.
     *
     * @param request  the {@link Request}
     * @param response the {@link Response}
     * @throws IOException should the {@link Request} be unsuccessful
     */
    protected void onUnsuccessfulRequest(final Request request,
                                         final Response response)
        throws IOException {

        throw new IOException("Request failed with " + response.code() + ". Failed to execute " + request);
    }

    /**
     * Handle when the execution of a {@link Request} failed with the specified {@link Throwable}, allowing
     * implementations to gracefully recover from said failures.
     * <p>
     * By default, this method simply re-throws the specified {@link Throwable} as a {@link RuntimeException},
     * if not already a {@link RuntimeException}.
     *
     * @param request   the {@link Request}
     * @param throwable the {@link Throwable}
     * @return the result if the {@link Request} was recoverable
     */
    protected T onRequestFailed(final Request request,
                                final Throwable throwable) {

        throw (throwable instanceof RuntimeException)
            ? (RuntimeException) throwable
            : new RuntimeException("Request Failed", throwable);
    }

    /**
     * Handle when the attempting to process the {@link Response}, including creating a result using
     * {@link #createResult(Response)} failed.
     * <p>
     * By default, this method closes the {@link Response}.
     *
     * @param request   the {@link Request}
     * @param response  the {@link Response}
     * @param throwable the {@link Throwable} that occurred while processing the {@link Response}
     */
    public void onUnprocessableResponse(final Request request,
                                        final Response response,
                                        final Throwable throwable) {

        // by default, we close the Response
        response.close();
    }

    @Override
    public T submit() {
        // obtain the Request to submit
        final var request = createRequest(this.httpUrlBuilder);

        // attempt to execute the request representing the command
        try {
            // notify that the request has been created
            onRequestCreated(request);

            // create the http call for the request
            final var call = httpClient()
                .newCall(request);

            // notify that the call has been created
            onCallCreated(call);

            // execute the call
            final var response = call.execute();

            // notify that the response has been received
            onResponseReceived(response);

            // attempt to process the response
            try {
                // handle when the response is considered unsuccessful
                if (!response.isSuccessful()) {
                    onUnsuccessfulRequest(request, response);
                }

                // attempt to create the result from the response
                final var result = createResult(response);

                // notify that the request was successful
                onSuccessfulRequest(request, response, result);

                return result;
            } catch (final Throwable throwable) {
                // notify that the response is unprocessable
                onUnprocessableResponse(request, response, throwable);

                throw throwable;
            }
        } catch (final Throwable recoverable) {
            // attempt to recover from the exception
            return onRequestFailed(request, recoverable);
        }
    }
}
