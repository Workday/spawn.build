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
import build.base.configuration.Option;
import build.base.flow.CompletingSubscriber;
import build.base.foundation.Strings;
import build.spawn.application.Application;
import build.spawn.application.Customizer;
import build.spawn.application.Platform;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * An {@link Customizer} that will only allow an {@link Application} to start when a specified
 * regular expression pattern is observed. Static helper methods are provided for use with stdout and stderr.
 *
 * @author michael.matthews
 * @since Mar-2022
 */
public class WaitFor
    implements Customizer<Application> {

    /**
     * The {@link CompletingSubscriber} to provide a {@link CompletableFuture} for the observable pattern.
     */
    private final CompletingSubscriber<String> subscriber;

    /**
     * The {@link CompletableFuture} to be completed when the pattern is observed.
     */
    private final CompletableFuture<?> onPatternObserved;

    /**
     * A function for providing the option wrapper for the observer.
     */
    private final Function<? super CompletingSubscriber<String>, Option> optionProvider;

    private WaitFor(final String pattern,
                    final Function<? super CompletingSubscriber<String>, Option> optionProvider) {

        final var normalized = Strings.isEmpty(pattern) ? ".*" : pattern;
        this.subscriber = new CompletingSubscriber<>();
        this.onPatternObserved = this.subscriber.when(string -> string.matches(normalized));
        this.optionProvider = optionProvider;
    }

    @Override
    public void onPreparing(final Platform platform,
                            final Class<? extends Application> applicationClass,
                            final ConfigurationBuilder configurationBuilder) {

        Optional.ofNullable(this.optionProvider.apply(this.subscriber))
            .ifPresent(configurationBuilder::add);
    }

    @Override
    public CompletableFuture<? extends Application> onStart(final Platform platform,
                                                            final Class<? extends Application> applicationClass,
                                                            final Application application) {

        return this.onPatternObserved.thenApply(__ -> application);
    }

    /**
     * Constructs a {@link WaitFor} on stdout with the specified pattern.
     *
     * @param pattern the regular expression pattern
     * @return a new {@link WaitFor}
     */
    public static WaitFor stdout(final String pattern) {
        return new WaitFor(pattern, StandardOutputSubscriber::of);
    }

    /**
     * Constructs a {@link WaitFor} on stderr with the specified pattern.
     *
     * @param pattern the regular expression pattern
     * @return a new {@link WaitFor}
     */
    public static WaitFor stderr(final String pattern) {
        return new WaitFor(pattern, StandardErrorSubscriber::of);
    }

    /**
     * Constructs a {@link WaitFor} with the specified pattern and option provider.
     *
     * @param pattern        the regular expression pattern
     * @param optionProvider function to provide an option
     * @return a new {@link WaitFor}
     */
    public static WaitFor of(final String pattern,
                             final Function<? super CompletingSubscriber<String>, Option> optionProvider) {

        return new WaitFor(pattern, optionProvider);
    }
}
