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

import build.base.foundation.unit.MemorySize;

/**
 * A {@link JDKOption} to specify a maximum heap size.
 *
 * @author brian.oliver
 * @since Nov-2018
 */
public class MaximumHeapSize
    extends AbstractHeapSize {

    /**
     * Constructs a {@link MaximumHeapSize}.
     *
     * @param units      the units of the {@link MemorySize}
     * @param memorySize the {@link MemorySize} for the units
     */
    private MaximumHeapSize(final int units, final MemorySize memorySize) {
        super(units, memorySize);
    }

    @Override
    protected String getOption() {
        return "-Xmx";
    }

    /**
     * Creates a {@link MaximumHeapSize}.
     *
     * @param units      the units of the {@link MemorySize}
     * @param memorySize the {@link MemorySize} for the units
     * @return a {@link MaximumHeapSize}
     */
    public static MaximumHeapSize of(final int units, final MemorySize memorySize) {
        return new MaximumHeapSize(units, memorySize);
    }

    @Override
    public String toString() {
        return "MaximumHeapSize{" + this.units + " " + this.memorySize.name() + "}";
    }
}
