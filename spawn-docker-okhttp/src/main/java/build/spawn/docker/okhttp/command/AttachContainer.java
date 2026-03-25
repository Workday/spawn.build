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

import build.base.configuration.Configuration;
import build.base.io.NullWriter;
import build.base.io.Terminal;
import build.spawn.docker.Container;
import jakarta.inject.Inject;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.PipedReader;
import java.io.Reader;
import java.io.Writer;
import java.util.concurrent.CompletableFuture;

/**
 * The {@code Docker Engine} {@link Command} to attach to a {@code Container} using the
 * <a href="https://docs.docker.com/engine/api/v1.41/#operation/ContainerAttach">Attach Container</a> command.
 *
 * @author brian.oliver
 * @since Jun-2021
 */
public class AttachContainer
    extends AbstractNonBlockingCommand<Terminal> {

    /**
     * The {@link Container} to which to attach.
     */
    @Inject
    private Container container;

    /**
     * Constructs a {@link AttachContainer} {@link Command}.
     *
     * @param configuration the {@link Configuration} to use when attaching
     */
    public AttachContainer(final Configuration configuration) {
    }

    @Override
    protected Request createRequest(final HttpUrl.Builder httpUrlBuilder) {
        return new Request.Builder()
            .url(httpUrlBuilder
                .addPathSegment("containers")
                .addPathSegment(this.container.id())
                .addPathSegment("attach")
                .build())
            .post(new FormBody.Builder()
                .add("logs", "true")
                .add("stream", "true")
                .add("stdin", "false")
                .add("stdout", "true")
                .add("stderr", "true")
                .build())
            .build();
    }

    @Override
    protected Terminal createResult(final Response response)
        throws IOException {

        // establish PipedReaders for stdout and stderr
        final var outputReader = new PipedReader();
        final var errorReader = new PipedReader();

        // TODO: establish PipedWriter for stdin

        // establish the Terminal
        final Terminal terminal = new Terminal() {
            @Override
            public Reader getOutputReader() {
                return outputReader;
            }

            @Override
            public Reader getErrorReader() {
                return errorReader;
            }

            @Override
            public Writer getInputWriter() {
                return NullWriter.get();
            }

            @Override
            public void close() {
                AttachContainer.this.close();
            }

            @Override
            public CompletableFuture<?> onClosed() {
                return AttachContainer.this.processing();
            }
        };

        // establish a FrameProcessor to redirect the I/O frames to the Terminal
        final var processor = new FrameProcessor(response, outputReader, errorReader);

        processor.processing()
            .whenComplete((_, error) -> {
                if (error == null) {
                    onProcessingCompletion();
                } else {
                    onProcessingError(error);
                }
            });

        final var name = "container-" + this.container.id().substring(this.container.id().length() - 8);

        Thread.ofVirtual()
            .name(name)
            .start(processor);

        return terminal;
    }
}
