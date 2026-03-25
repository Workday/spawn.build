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
import build.base.io.NullWriter;
import build.spawn.application.Console;

import java.io.Reader;
import java.io.Writer;

/**
 * A {@link Console} that returns end-of-file when attempting to read from stdin and ignores all output sent to
 * stdout and stderr.
 *
 * @author brian.oliver
 * @since Oct-2018
 */
public class NullConsole
    implements Console {

    @Override
    public Writer getOutputWriter() {
        return NullWriter.get();
    }

    @Override
    public Reader getInputReader() {
        return NullReader.get();
    }

    @Override
    public Writer getErrorWriter() {
        return NullWriter.get();
    }

    @Override
    public void close() {
        // there's nothing to do when closing
    }

    /**
     * Obtains a {@link Console.Supplier} of {@link NullConsole}s.
     *
     * @return a {@link Console.Supplier} for {@link NullConsole}s
     */
    public static Console.Supplier supplier() {
        return optionsByType -> new NullConsole();
    }
}
