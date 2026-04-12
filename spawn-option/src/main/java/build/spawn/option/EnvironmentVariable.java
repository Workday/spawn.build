package build.spawn.option;

/*-
 * #%L
 * Spawn Option
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

import build.base.configuration.MappedOption;
import build.base.configuration.Option;
import build.base.expression.compat.Processor;
import build.base.expression.option.ResolvableOption;
import build.base.table.Table;
import build.base.table.Tabular;
import build.base.table.option.CellSeparator;
import build.base.table.option.RowComparator;
import build.base.table.option.TableName;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * An immutable {@link Option} to represent an environment variable.
 *
 * @author brian.oliver
 * @since Nov-2018
 */
public class EnvironmentVariable
    implements MappedOption<String>, ResolvableOption<EnvironmentVariable>, Tabular {

    /**
     * The key for the {@link EnvironmentVariable}.
     */
    private final String key;

    /**
     * The {@link Optional} value for the {@link EnvironmentVariable}.
     */
    private final Optional<String> value;

    /**
     * Constructs a {@link EnvironmentVariable}.
     *
     * @param key   the key
     * @param value the value (may be {code null})
     */
    private EnvironmentVariable(final String key, final String value) {
        Objects.requireNonNull(key, "The key for an EnvironmentVariable must not be null");

        this.key = key;
        this.value = Optional.ofNullable(value);
    }

    @Override
    public String key() {
        return this.key;
    }

    /**
     * Obtains the {@link Optional} value of the {@link EnvironmentVariable}.
     *
     * @return the {@link Optional} value
     */
    public Optional<String> value() {
        return this.value;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        final EnvironmentVariable that = (EnvironmentVariable) other;
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
            table.options().add(TableName.of("Environment Variables"));
            table.options().add(RowComparator.orderByColumn(0));
            table.options().add(CellSeparator.of(" = "));
            return table;
        });
    }

    @Override
    public EnvironmentVariable resolve(final Processor processor) {
        if (value().isPresent()) {
            final String expression = processor.replace(value().get());
            if (!expression.equals(value().get())) {
                return EnvironmentVariable.of(key(), expression);
            }
        }
        return this;
    }

    @Override
    public void tabularize(final Table table) {
        table.addRow(this.key, this.value.orElse(""));
    }

    /**
     * Creates a {@link EnvironmentVariable} with only the specified key.
     *
     * @param key the key
     * @return a {@link EnvironmentVariable}
     */
    public static EnvironmentVariable of(final String key) {
        return new EnvironmentVariable(key, null);
    }

    /**
     * Creates a {@link EnvironmentVariable} with the specified key and value.  Should the value be {@code null},
     * it's equivalent to calling {@link #of(String)}.
     *
     * @param key   the key
     * @param value the value (may be {code null})
     * @return a {@link EnvironmentVariable}
     */
    public static EnvironmentVariable of(final String key, final String value) {
        return new EnvironmentVariable(key, value);
    }

    /**
     * Creates a {@link EnvironmentVariable} with the specified key and value.  Should the value be {@code null}, it's
     * equivalent to calling {@link #of(String)}.
     *
     * @param key   the key
     * @param value the value
     * @return a {@link EnvironmentVariable}
     */
    public static EnvironmentVariable of(final String key, final Object value) {
        return new EnvironmentVariable(key, value == null ? null : value.toString());
    }
}
