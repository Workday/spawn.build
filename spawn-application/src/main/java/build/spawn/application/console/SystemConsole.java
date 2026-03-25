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

import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * A {@link Console} that redirects stdout and stderr to the {@link System}.
 *
 * @author brian.oliver
 * @since Oct-2018
 */
public class SystemConsole
    implements Console {

    /**
     * The {@link Writer} for stdout.
     */
    private final Writer outputWriter;

    /**
     * The {@link Writer} for stderr.
     */
    private final Writer errorWriter;

    /**
     * Constructs a {@link SystemConsole}.
     */
    private SystemConsole() {
        this.outputWriter = new OutputStreamWriter(System.out);
        this.errorWriter = new OutputStreamWriter(System.err);
    }

    @Override
    public Writer getOutputWriter() {
        return this.outputWriter;
    }

    @Override
    public Reader getInputReader() {
        return NullReader.get();
    }

    @Override
    public Writer getErrorWriter() {
        return this.errorWriter;
    }

    @Override
    public void close() {
        // there's nothing to do when closing a SystemConsole
    }

    /**
     * Obtains a {@link Console.Supplier} of {@link SystemConsole}s.
     *
     * @return a {@link Console.Supplier} for {@link SystemConsole}s
     */
    public static Console.Supplier supplier() {
        return _ -> new SystemConsole();
    }
}
