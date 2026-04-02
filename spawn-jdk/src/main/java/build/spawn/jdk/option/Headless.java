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

import build.base.configuration.ConfigurationBuilder;
import build.base.configuration.Default;
import build.spawn.application.Platform;
import build.spawn.jdk.JDKApplication;

import java.util.stream.Stream;

/**
 * An immutable {@link JDKOption} to define if a {@link JDKApplication} should be
 * launched in <a href="https://www.oracle.com/technetwork/articles/javase/headless-136834.html">headless</a> mode.
 *
 * @author brian.oliver
 * @since Oct-2018
 */
public enum Headless
    implements JDKOption {

    /**
     * A {@link Headless} constant representing enabled mode.
     */
    @Default
    ENABLED,

    /**
     * A {@link Headless} constant representing disabled mode.
     */
    DISABLED;

    @Override
    public Stream<String> resolve(final Platform platform,
                                  final ConfigurationBuilder options) {

        return this == ENABLED ? Stream.of("-Djava.awt.headless=true") : Stream.empty();
    }

    @Override
    public String toString() {
        return "Headless{" + name() + "}";
    }
}
