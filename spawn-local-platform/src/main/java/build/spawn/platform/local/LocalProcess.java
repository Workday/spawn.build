package build.spawn.platform.local;

/*-
 * #%L
 * Spawn Local Platform
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

import build.base.io.Terminal;
import build.spawn.application.AbstractProcess;
import build.spawn.application.Process;
import build.spawn.application.option.Argument;

import java.net.InetAddress;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * A {@link Process} created by a {@link LocalMachine}.
 *
 * @author brian.oliver
 * @since Oct-2018
 */
public class LocalProcess
    extends AbstractProcess {

    /**
     * The underlying native {@link Process} that this {@link LocalProcess} represents and controls.
     */
    private final java.lang.Process process;

    /**
     * The {@link LocalMachine} the {@link LocalProcess} was/is/will be running on
     */
    private final LocalMachine machine;

    /**
     * The {@link CompletableFuture} to complete upon termination of the {@link LocalProcess}.
     */
    private final CompletableFuture<Process> onExit;

    /**
     * The daemon {@link Thread} to wait for this {@link java.lang.Process} to terminate.
     */
    private final Thread waitThread;

    /**
     * Constructs a {@link LocalProcess} representing an underlying Java Platform {@link Process}.
     *
     * @param process the {@link Process}
     * @param machine the {@link LocalMachine} the {@link Process} is to be run on
     */
    public LocalProcess(final java.lang.Process process,
                        final LocalMachine machine) {

        super(Terminal.of(Objects.requireNonNull(process, "The native Process must not be null")));

        this.process = process;
        this.machine = Objects.requireNonNull(machine, "The LocalMachine must not be null");
        this.onExit = new CompletableFuture<>();

        // start a virtual thread to wait for the Process to terminate
        this.waitThread = Thread.ofVirtual()
            .name("LocalProcess Watcher")
            .start(() -> {
                try {
                    this.process.waitFor();
                    this.onExit.complete(this);
                }
                catch (final InterruptedException e) {
                    this.onExit.completeExceptionally(e);
                }
            });
    }

    @Override
    public Stream<InetAddress> addresses() {
        return this.machine.addresses();
    }

    @Override
    public long pid() {
        return this.process.pid();
    }

    @Override
    public CompletableFuture<Process> onExit() {
        return this.onExit;
    }

    @Override
    public boolean isAlive() {
        return this.process.isAlive();
    }

    @Override
    public OptionalInt exitValue() {
        try {
            return OptionalInt.of(this.process.exitValue());
        }
        catch (final IllegalThreadStateException e) {
            return OptionalInt.empty();
        }
    }

    @Override
    public CompletableFuture<Process> suspend() {
        return this.machine.launch("kill", Argument.of("-STOP"), Argument.of(pid()))
            .onExit()
            .thenApply(__ -> this);
    }

    @Override
    public CompletableFuture<Process> resume() {
        return this.machine.launch("kill", Argument.of("-CONT"), Argument.of(pid()))
            .onExit()
            .thenApply(__ -> this);
    }

    @Override
    public void shutdown() {
        this.process.destroy();

        super.shutdown();
    }

    @Override
    public void destroy() {
        this.process.destroyForcibly();

        super.destroy();
    }
}
