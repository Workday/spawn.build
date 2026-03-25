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

import build.base.configuration.ConfigurationBuilder;
import build.base.configuration.Default;
import build.base.configuration.Option;
import build.spawn.application.Application;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * An {@link Option} defining a mechanism to produce a {@link DiagnosticName} when launching an {@link Application}
 * using the launch {@link ConfigurationBuilder} and {@link Application} {@link Name}.
 *
 * @author brian.oliver
 * @since Jan-2025
 */
public class DiagnosticNameProvider
    implements Option {

    private final BiFunction<ConfigurationBuilder, Name, DiagnosticName> biFunction;

    /**
     * Constructs a {@link DiagnosticNameProvider} given a {@link BiFunction} that can create {@link DiagnosticName}s
     * based on a launch {@link ConfigurationBuilder} and an {@link Application} {@link Name}.
     *
     * @param biFunction the {@link BiFunction}
     */
    private DiagnosticNameProvider(final BiFunction<ConfigurationBuilder, Name, DiagnosticName> biFunction) {
        this.biFunction = Objects.requireNonNull(biFunction, "The DiagnosticNameProvider Function must not be null");
    }

    /**
     * Creates a {@link DiagnosticName} given the launch {@link ConfigurationBuilder} and {@link Application}
     * {@link Name}.
     *
     * @param builder the launch {@link ConfigurationBuilder}
     * @param name    the {@link Application} {@link Name}
     * @return a new {@link DiagnosticName}
     */
    public DiagnosticName create(final ConfigurationBuilder builder,
                                 final Name name) {

        return this.biFunction.apply(builder, name);
    }

    /**
     * Creates a {@link DiagnosticNameProvider} given the specified {@link BiFunction} to create {@link DiagnosticName}s
     * from {@link ConfigurationBuilder}s and {@link Application} {@link Name}s.
     *
     * @param biFunction the {@link BiFunction}
     * @return a new {@link DiagnosticNameProvider}
     */
    public static DiagnosticNameProvider of(final BiFunction<ConfigurationBuilder, Name, DiagnosticName> biFunction) {
        return new DiagnosticNameProvider(biFunction);
    }

    /**
     * The default {@link DiagnosticNameProvider} which only uses the {@link Application} {@link Name}.
     *
     * @return the default {@link DiagnosticNameProvider}
     */
    @Default
    public static DiagnosticNameProvider automatic() {
        return new DiagnosticNameProvider((_, name) -> DiagnosticName.of(name.get()));
    }
}
