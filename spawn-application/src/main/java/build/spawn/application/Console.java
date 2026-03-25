package build.spawn.application;

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

import build.base.configuration.Configuration;
import build.base.configuration.Option;
import build.base.configuration.OptionDiscriminator;
import build.spawn.application.console.NullConsole;
import build.spawn.application.console.PrintWriterConsole;
import build.spawn.application.console.SystemConsole;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * A {@link Console} defines {@link Reader}s and {@link Writer}s for an {@link Application}, allowing interaction
 * and programmatic redirection of stdin, stdout and stderr.
 *
 * @author Brian Oliver
 * @since Jan-2018
 */
public interface Console
    extends AutoCloseable {

    /**
     * Obtains the {@link Writer} for writing to standard output.
     *
     * @return a {@link Writer}
     */
    Writer getOutputWriter();

    /**
     * Obtains the {@link Reader} for reading from standard input.
     *
     * @return a {@link Reader}
     */
    Reader getInputReader();

    /**
     * Obtains the {@link Writer} for writing to standard error.
     *
     * @return a {@link Writer}
     */
    Writer getErrorWriter();

    /**
     * Obtains a {@link Supplier} for the system, equivalent to invoking {@link SystemConsole#supplier()}.
     *
     * @return a {@link Supplier} for the {@link SystemConsole}.
     */
    static Supplier ofSystem() {
        return Supplier.ofSystem();
    }

    /**
     * Obtains a {@link Supplier} for the {@link NullConsole}, equivalent to invoking
     * {@link NullConsole#supplier()}.
     *
     * @return a {@link Supplier} for the {@link NullConsole}.
     */
    static Supplier none() {
        return Supplier.none();
    }

    /**
     * Obtains a {@link Supplier} for a {@link PrintWriterConsole} using the specified {@link PrintWriter}.
     *
     * @param writer the {@link PrintWriter}
     * @return a {@link Supplier} for the {@link PrintWriterConsole}.
     */
    static Supplier of(final PrintWriter writer) {
        return Supplier.of(writer);
    }

    /**
     * An {@link Option} to define a supplier of a {@link Console} when launching an {@link Application}
     */
    @OptionDiscriminator
    interface Supplier
        extends Option {

        /**
         * Creates a {@link Console} for an {@link Application} with the provided launch {@link Option}s.
         *
         * @param configuration the {@link Configuration} used
         * @return a new {@link Console}
         */
        Console create(Configuration configuration);

        /**
         * Obtains a {@link Supplier} for the system, equivalent to invoking {@link SystemConsole#supplier()}.
         *
         * @return a {@link Supplier} for the {@link SystemConsole}.
         */
        static Supplier ofSystem() {
            return SystemConsole.supplier();
        }

        /**
         * Obtains a {@link Supplier} for the {@link NullConsole}, equivalent to invoking
         * {@link NullConsole#supplier()}.
         *
         * @return a {@link Supplier} for the {@link NullConsole}.
         */
        static Supplier none() {
            return NullConsole.supplier();
        }

        /**
         * Obtains a {@link Supplier} for a {@link PrintWriterConsole} using the specified {@link PrintWriter}.
         *
         * @param writer the {@link PrintWriter}
         * @return a {@link Supplier} for the {@link PrintWriterConsole}.
         */
        static Supplier of(final PrintWriter writer) {
            return PrintWriterConsole.supplier(writer);
        }
    }
}
