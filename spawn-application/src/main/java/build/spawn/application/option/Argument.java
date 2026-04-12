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

import build.base.configuration.AbstractValueOption;
import build.base.configuration.CollectedOption;
import build.base.configuration.Option;
import build.base.expression.compat.Processor;
import build.base.expression.option.ResolvableOption;
import build.spawn.application.Application;

import java.util.List;

/**
 * An {@link Option} to define an argument for an {@link Application} to launch.
 *
 * @author brian.oliver
 * @since Oct-2018
 */
public class Argument
    extends AbstractValueOption<String>
    implements CollectedOption<List>, ResolvableOption<Argument> {

    /**
     * Constructs an {@link Argument}.
     *
     * @param value the value
     */
    private Argument(final String value) {
        super(value);
    }

    /**
     * Creates an {@link Argument}.
     *
     * @param value the value
     * @return an {@link Argument}
     */
    public static Argument of(final String value) {
        return new Argument(value);
    }

    /**
     * Creates an {@link Argument}.
     *
     * @param value the value
     * @return an {@link Argument}
     */
    public static Argument of(final Object value) {
        return new Argument(value == null ? "null" : value.toString());
    }

    @Override
    public Argument resolve(final Processor processor) {
        final var expression = processor.replace(get());
        if (expression.equals(get())) {
            return this;
        }
        return Argument.of(expression);
    }
}
