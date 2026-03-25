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
import build.base.foundation.unit.MemorySize;
import build.spawn.application.Platform;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * An abstract {@link JDKOption} representing a Virtual Machine {@link AbstractHeapSize}.
 *
 * @author brian.oliver
 * @since Nov-2018
 */
public abstract class AbstractHeapSize
    implements JDKOption {

    /**
     * The number of units of the {@link MemorySize}.
     */
    protected final int units;

    /**
     * The {@link MemorySize} for the units.
     */
    protected final MemorySize memorySize;

    /**
     * Constructs a {@link AbstractHeapSize}.
     *
     * @param units      the units of the {@link MemorySize}
     * @param memorySize the {@link MemorySize} for the units
     */
    protected AbstractHeapSize(final int units, final MemorySize memorySize) {

        if (units < 0) {
            throw new IllegalArgumentException("HeapSize must not be negative. (" + units + " was specified)");
        }
        else if (memorySize == null) {
            throw new IllegalArgumentException("MemorySize must not be null");
        }

        this.units = units;
        this.memorySize = memorySize;
    }

    /**
     * Obtains the command-line option used to specify a {@link AbstractHeapSize}.
     * <p>
     * The amount of memory will be appended to this in order to create the {@link AbstractHeapSize} for a
     * {@link build.spawn.jdk.JDKApplication}.
     *
     * @return the command-line option
     */
    protected abstract String getOption();

    @Override
    public Stream<String> resolve(final Platform platform, final ConfigurationBuilder options) {
        return Stream.of(getOption() + this.units + this.memorySize.name().toLowerCase().substring(0, 1));
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        final AbstractHeapSize heapSize = (AbstractHeapSize) other;
        return this.units == heapSize.units && this.memorySize == heapSize.memorySize;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.units, this.memorySize);
    }

}
