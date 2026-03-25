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

import build.base.configuration.ConfigurationBuilder;
import build.base.configuration.Option;

/**
 * An abstract {@link Specification} to support the development of fluent-style {@link Application} configuration.
 *
 * @param <A> the type of {@link Application}
 * @param <S> the type of {@link AbstractSpecification}
 * @author brian.oliver
 * @since Aug-2021
 */
public abstract class AbstractSpecification<A extends Application, S extends AbstractSpecification<A, S>>
    implements Specification<A> {

    /**
     * The {@link ConfigurationBuilder} for the {@link Specification}.
     */
    private final ConfigurationBuilder configurationBuilder;

    /**
     * Constructs an {@link AbstractSpecification}.
     */
    public AbstractSpecification() {
        this.configurationBuilder = ConfigurationBuilder.create();
    }

    @Override
    public ConfigurationBuilder options() {
        return this.configurationBuilder;
    }

    /**
     * Adds the specified {@link Option} to the {@link Specification}.
     *
     * @param option the {@link Option}
     * @return this {@link Specification} to permit fluent-style method invocation
     */
    @SuppressWarnings("unchecked")
    public S with(final Option option) {
        this.configurationBuilder.add(option);
        return (S) this;
    }

    /**
     * Adds the specified {@link Option}s to the {@link Specification}.
     *
     * @param options the {@link Option}s
     * @return this {@link Specification} to permit fluent-style method invocation
     */
    @SuppressWarnings("unchecked")
    public S with(final Option... options) {
        this.configurationBuilder.include(options);
        return (S) this;
    }

    /**
     * Specifies the {@link Console.Supplier} for the {@link Specification}.
     *
     * @param supplier the {@link Console.Supplier}
     * @return this {@link Specification} to permit fluent-style method invocation
     */
    @SuppressWarnings("unchecked")
    public S withConsole(final Console.Supplier supplier) {
        return supplier == null ? (S) this : with(supplier);
    }
}
