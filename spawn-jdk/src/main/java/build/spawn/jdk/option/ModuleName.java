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

import java.util.Objects;

/**
 * A value type representing the name of a JDK module.
 *
 * @author brian.oliver
 * @since Jan-2025
 */
public class ModuleName {

    /**
     * The {@code ALL-DEFAULT} {@link ModuleName}.
     */
    public static final ModuleName ALL_DEFAULT = new ModuleName("ALL-DEFAULT");

    /**
     * The {@code ALL-SYSTEM} {@link ModuleName}.
     */
    public static final ModuleName ALL_SYSTEM = new ModuleName("ALL-SYSTEM");

    /**
     * The {@code ALL-MODULE-PATH} {@link ModuleName}.
     */
    public static final ModuleName ALL_MODULE_PATH = new ModuleName("ALL-MODULE-PATH");

    /**
     * The module name.
     */
    private final String moduleName;

    /**
     * Constructs a {@link ModuleName}.
     *
     * @param moduleName the module name
     */
    private ModuleName(final String moduleName) {
        this.moduleName = Objects.requireNonNull(moduleName, "The Module Name must not be null")
            .trim();

        if (this.moduleName.isEmpty()) {
            throw new IllegalArgumentException("The Module Name must not be empty");
        }
    }

    /**
     * Obtains the {@link String} representation of the {@link ModuleName}.
     *
     * @return the {@link String} representation
     */
    public String get() {
        return this.moduleName;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof final ModuleName that)) {
            return false;
        }
        return Objects.equals(this.moduleName, that.moduleName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.moduleName);
    }

    @Override
    public String toString() {
        return this.moduleName;
    }

    /**
     * Creates a {@link ModuleName} given a name.
     *
     * @param moduleName the module name
     * @return a new {@link ModuleName}
     */
    public static ModuleName of(final String moduleName) {
        // TODO: ensure the ModuleName is valid (eg: no spaces)
        return new ModuleName(moduleName);
    }
}
