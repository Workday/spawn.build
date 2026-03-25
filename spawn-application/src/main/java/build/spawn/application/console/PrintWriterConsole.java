package build.spawn.application.console;

/*-
 * #%L
 * Spawn Application
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

import build.base.io.NullReader;
import build.spawn.application.Console;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Objects;

/**
 * A {@link Console} that saves stdout and stderr to a {@link PrintWriter}.
 *
 * @author brian.oliver
 * @since Jan-2021
 */
public class PrintWriterConsole
    implements Console {

    /**
     * The {@link PrintWriter} to which to output the stdout and stderr.
     */
    private final PrintWriter writer;

    /**
     * Constructs a {@link PrintWriterConsole} for the specified {@link PrintWriter}.
     *
     * @param writer the {@link PrintWriter}
     */
    private PrintWriterConsole(final PrintWriter writer) {
        this.writer = Objects.requireNonNull(writer, "The writer must not be null");
    }

    @Override
    public Writer getOutputWriter() {
        return this.writer;
    }

    @Override
    public Reader getInputReader() {
        return NullReader.get();
    }

    @Override
    public Writer getErrorWriter() {
        return this.writer;
    }

    @Override
    public void close() {

        // we don't close the PrintWriter provided to us as we don't own it
    }

    /**
     * Creates a {@link Console} {@link Supplier} for the specified {@link PrintWriter}.
     *
     * @param writer the {@link PrintWriter}
     * @return a new {@link Console} {@link Supplier}
     */
    public static Supplier supplier(final PrintWriter writer) {
        return __ -> new PrintWriterConsole(writer);
    }
}
