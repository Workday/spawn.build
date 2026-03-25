package build.spawn.application.option;

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
import build.base.configuration.Default;
import build.base.configuration.Option;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * An immutable {@link Option} to specify an output transformer {@link Function} for stdout.
 *
 * @author brian.oliver
 * @since Nov-2018
 */
@FunctionalInterface
public interface StandardOutputFormatter
    extends Option {

    /**
     * Obtains a {@link Function} to transform each line of output into another format.
     *
     * @param configuration the {@link Configuration}
     * @return a new transformer {@link Function}
     */
    Function<String, String> getTransformer(Configuration configuration);

    /**
     * Obtains the {@link StandardOutputFormatter} that does not perform any output transformation.
     *
     * @return a {@link StandardOutputFormatter}
     */
    static StandardOutputFormatter none() {
        return _ -> line -> line;
    }

    /**
     * Obtains the default {@link StandardOutputFormatter}.
     *
     * @return the default {@link StandardOutputFormatter}
     */
    @Default
    static StandardOutputFormatter getDefault() {

        return configuration -> {
            final var name = configuration.getValue(DiagnosticName.class);
            final var lineNumber = new AtomicInteger(1);

            return line -> String.format("[%s:%5d] %s", name, lineNumber.getAndIncrement(), line);
        };
    }
}
