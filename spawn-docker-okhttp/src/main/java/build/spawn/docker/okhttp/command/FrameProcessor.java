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

import okhttp3.Response;

import java.io.EOFException;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.CompletableFuture;

/**
 * Asynchronously processes the {@link Response} {@code stdin}, {@code stdout} and {@code stderr} frames produced
 * by a {@link build.spawn.docker.Container}.
 *
 * @author brian.oliver
 * @since Jun-2021
 */
class FrameProcessor
    implements Runnable {

    /**
     * The frame header size.
     */
    private static final int HEADER_SIZE = 8;

    /**
     * The {@link Response} to process
     */
    private final Response response;

    /**
     * The {@link CompletableFuture} when Frame processing has completed.
     */
    private final CompletableFuture<?> processing;

    /**
     * The {@link PipedWriter} to which stdout frames will be output.
     */
    private final PipedWriter outputWriter;

    /**
     * The {@link PipedWriter} to which stderr frames will be output.
     */
    private final PipedWriter errorWriter;

    /**
     * Constructs a {@link FrameProcessor}.
     *
     * @param response     the {@link Response} to process
     * @param outputReader the {@link PipedReader} to which stdout frames will be output
     * @param errorReader  the {@link PipedReader} to which stderr frames will be output
     */
    FrameProcessor(final Response response,
                   final PipedReader outputReader,
                   final PipedReader errorReader)
        throws IOException {

        this.response = response;
        this.processing = new CompletableFuture<>();
        this.outputWriter = new PipedWriter(outputReader);
        this.errorWriter = new PipedWriter(errorReader);
    }

    /**
     * Obtains the {@link CompletableFuture} indicating when the {@link FrameProcessor} has completed, exceptionally
     * or otherwise.
     *
     * @return the {@link CompletableFuture}
     */
    public CompletableFuture<?> processing() {
        return this.processing;
    }

    @Override
    public void run() {
        try (var inputStream = this.response.body().byteStream()) {
            while (!this.processing.isDone()) {
                // read the frame header
                final var header = new byte[HEADER_SIZE];
                int currentSize = 0;

                do {
                    final var size = inputStream.read(header, currentSize, HEADER_SIZE - currentSize);

                    if (size == -1) {
                        this.processing.complete(null);
                    }

                    currentSize += size;
                } while (!this.processing.isDone() && currentSize < HEADER_SIZE);

                // read the frame payload
                if (!this.processing.isDone()) {

                    // determine the Writer based on the type of Frame
                    final var writer = header[0] == 1 ? this.outputWriter : this.errorWriter;

                    // determine the payload size
                    final var payloadSize = ((header[4] & 0xff) << 24)
                        + ((header[5] & 0xff) << 16)
                        + ((header[6] & 0xff) << 8)
                        + (header[7] & 0xff);

                    // attempt to read the payload
                    final var payload = new byte[payloadSize];
                    var currentPayloadSize = 0;

                    try {
                        do {
                            final var size = inputStream.read(payload, currentPayloadSize,
                                payloadSize - currentPayloadSize);

                            if (size == -1) {
                                if (currentPayloadSize != payloadSize) {
                                    throw new IOException(
                                        String.format("payload must be %d bytes long, but was %d",
                                            payloadSize,
                                            currentPayloadSize));
                                }
                                break;
                            }
                            currentPayloadSize += size;
                        } while (currentPayloadSize < payloadSize);
                    } catch (final IOException e) {
                        // we ignore the IOException that may have occurred while reading
                        // so we can write what may have been read
                        // (the next attempt to read will naturally fail and thus terminate)
                    }

                    try {
                        // output the payload to Writer
                        final var content = new String(payload);
                        writer.write(content);
                        writer.flush();
                    } catch (final IOException e) {
                        this.processing.completeExceptionally(e);
                    }
                }
            }
        } catch (final SocketException | SocketTimeoutException | EOFException e) {
            this.processing.complete(null);

        } catch (final Exception e) {
            this.processing.completeExceptionally(e);
        }

        // close the stdout Writer
        try {
            this.outputWriter.close();
        } catch (final IOException e) {
            // we ignore close failures (there's nothing we can do)
        }

        // close the stderr Writer
        try {
            this.errorWriter.close();
        } catch (final IOException e) {
            // we ignore close failures (there's nothing we can do)
        }
    }
}
