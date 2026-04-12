package build.spawn.jdk.option;

/*-
 * #%L
 * Spawn JDK
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
import build.base.configuration.Configuration;
import build.base.configuration.ConfigurationBuilder;
import build.base.configuration.Option;
import build.base.expression.compat.Processor;
import build.base.expression.option.ResolvableOption;
import build.base.option.JDKVersion;
import build.spawn.application.Platform;
import build.spawn.jdk.JDKApplication;

import java.util.List;
import java.util.stream.Stream;

/**
 * A {@link JDKApplication} specific {@link Configuration} {@link Option}.
 *
 * @author brian.oliver
 * @since Oct-2018
 */
public interface JDKOption
    extends Option {

    /**
     * Determines if the {@link JDKOption} is supported by the specified {@link JDKVersion} with the provided
     * {@link ConfigurationBuilder} {@link Option}s.  {@link JDKOption}s that are not supported will not be included
     * when launching a {@link JDKApplication}.
     *
     * @param jdkVersion the {@link JDKVersion}
     * @param options    the {@link ConfigurationBuilder}
     * @return {@code true} if the {@link JDKOption} is supported, {@code false} otherwise.
     */
    default boolean isSupported(final JDKVersion jdkVersion,
                                final ConfigurationBuilder options) {
        return true;
    }

    /**
     * Resolves a {@link JDKOption} for the specified {@link Platform}, producing a {@link Stream} of {@link String}s,
     * each to be added to the commandline to launch a {@link JDKApplication}.
     *
     * @param platform the {@link Platform}
     * @param options  the {@link ConfigurationBuilder}
     * @return a {@link Stream} of {@link String}s
     */
    Stream<String> resolve(Platform platform,
                           ConfigurationBuilder options);

    /**
     * Constructs a custom {@link JDKOption} with the specified value.
     *
     * @param value the value
     * @return a custom {@link JDKOption}
     */
    static JDKOption of(final String value) {
        return new Custom(value);
    }

    /**
     * A custom {@link JDKOption} that returns a provided {@link String} that can be resolved by using a {@link Processor}.
     */
    class Custom
        extends AbstractValueOption<String>
        implements CollectedOption<List>, JDKOption, ResolvableOption<JDKOption> {

        /**
         * Constructs a {@link Custom} {@link JDKOption}.
         *
         * @param value the value
         */
        private Custom(final String value) {
            super(value);
        }

        @Override
        public Stream<String> resolve(final Platform platform,
                                      final ConfigurationBuilder options) {
            return Stream.of(get());
        }

        @Override
        public JDKOption resolve(final Processor processor) {
            final var currentValue = get();
            final var resolvedValue = processor.replace(get());

            return currentValue.equals(resolvedValue)
                ? this
                : JDKOption.of(resolvedValue);
        }
    }
}
