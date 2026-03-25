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
import build.base.configuration.MappedOption;
import build.base.configuration.Option;
import build.base.expression.Processor;
import build.base.expression.option.ResolvableOption;
import build.base.foundation.Strings;
import build.base.table.Table;
import build.base.table.Tabular;
import build.base.table.option.CellSeparator;
import build.base.table.option.RowComparator;
import build.base.table.option.TableName;
import build.spawn.application.Platform;
import build.spawn.jdk.JDKApplication;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * An immutable {@link Option} to represent {@link JDKApplication} system property.
 *
 * @author brian.oliver
 * @since Oct-2018
 */
public class SystemProperty
    implements JDKOption, MappedOption<String>, ResolvableOption<SystemProperty>, Tabular {

    /**
     * The key for the {@link SystemProperty}.
     */
    private final String key;

    /**
     * The {@link Optional} value for the {@link SystemProperty}.
     */
    private final Optional<String> value;

    /**
     * Constructs a {@link SystemProperty}.
     *
     * @param key   the key
     * @param value the value (may be {code null})
     */
    private SystemProperty(final String key,
                           final String value) {

        Objects.requireNonNull(key, "The key for a SystemProperty must not be null");

        this.key = key;
        this.value = Optional.ofNullable(value);
    }

    @Override
    public String key() {
        return this.key;
    }

    /**
     * Obtains the {@link Optional} value of the {@link SystemProperty}.
     *
     * @return the {@link Optional} value
     */
    public Optional<String> value() {
        return this.value;
    }

    @Override
    public Stream<String> resolve(final Platform platform, final ConfigurationBuilder options) {
        final StringBuilder builder = new StringBuilder();

        builder.append("-D");
        builder.append(this.key);

        this.value.ifPresent(value -> {
            builder.append("=");
            builder.append(Strings.doubleQuoteIfContainsWhiteSpace(value));
        });

        return Stream.of(builder.toString());
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        final SystemProperty that = (SystemProperty) other;
        return Objects.equals(this.key, that.key) && Objects.equals(this.value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.key, this.value);
    }

    @Override
    public Optional<Supplier<Table>> getTableSupplier() {
        return Optional.of(() -> {
            final Table table = Table.create();
            table.options().add(TableName.of("System Properties"));
            table.options().add(RowComparator.orderByColumn(0));
            table.options().add(CellSeparator.of(" = "));
            return table;
        });
    }

    @Override
    public SystemProperty resolve(final Processor processor) {
        return value()
            .map(value -> {
                final var expression = processor.replace(value);
                return expression.equals(value)
                    ? this
                    : SystemProperty.of(key(), expression);
            })
            .orElse(this);
    }

    @Override
    public void tabularize(final Table table) {
        table.addRow(this.key, this.value.orElse(""));
    }

    /**
     * Creates a {@link SystemProperty} with only the specified key.
     *
     * @param key the key
     * @return a {@link SystemProperty}
     */
    public static SystemProperty of(final String key) {
        return new SystemProperty(key, null);
    }

    /**
     * Creates a {@link SystemProperty} with the specified key and value.  Should the value be {@code null},
     * it's equivalent to calling {@link #of(String)}.
     *
     * @param key   the key
     * @param value the value (may be {code null})
     * @return a {@link SystemProperty}
     */
    public static SystemProperty of(final String key, final String value) {
        return new SystemProperty(key, value);
    }

    /**
     * Creates a {@link SystemProperty} with the specified key and value.  Should the value be {@code null}, it's
     * equivalent to calling {@link #of(String)}.
     *
     * @param key   the key
     * @param value the value
     * @return a {@link SystemProperty}
     */
    public static SystemProperty of(final String key, final Object value) {
        return new SystemProperty(key, value == null ? null : value.toString());
    }
}
