package build.spawn.application.composition.option;

/*-
 * #%L
 * Spawn Application Composition
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
import build.base.configuration.Option;
import build.base.expression.compat.Resolvable;
import build.base.expression.compat.Variable;
import build.spawn.application.Application;
import build.spawn.application.composition.Composition;

/**
 * An immutable {@link Option} that identifies a launched {@link Application} within a {@link Composition}.
 *
 * @author brian.oliver
 * @since jun-2020
 */
public class ApplicationIdentifier
    extends AbstractValueOption<Integer>
    implements Resolvable<Integer> {

    /**
     * The system property representing an {@link ApplicationIdentifier}.
     */
    public final static String SYSTEM_PROPERTY = "build.spawn.application.id";

    /**
     * The {@link Variable} name.
     */
    static final String VARIABLE_NAME = "application.id";

    /**
     * Constructs an {@link ApplicationIdentifier}.
     *
     * @param identifier the identifier
     */
    private ApplicationIdentifier(final int identifier) {
        super(identifier);
    }

    /**
     * Obtains an {@link ApplicationIdentifier} for an {@link Application} for a {@link Composition}.
     *
     * @param identity the identifier
     * @return an {@link ApplicationIdentifier}
     */
    public static ApplicationIdentifier of(final int identity) {
        return new ApplicationIdentifier(identity);
    }

    @Override
    public String name() {
        return VARIABLE_NAME;
    }

    @Override
    public Integer value() {
        return get();
    }
}
