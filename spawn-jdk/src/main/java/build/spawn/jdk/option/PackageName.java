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
 * A value type representing the name of a Java package.
 *
 * @author reed.vonredwitz
 * @since May-2026
 */
public class PackageName {

    /**
     * The package name.
     */
    private final String packageName;

    /**
     * Constructs a {@link PackageName}.
     *
     * @param packageName the package name
     */
    private PackageName(final String packageName) {
        this.packageName = Objects.requireNonNull(packageName, "The Package Name must not be null")
            .trim();

        if (this.packageName.isEmpty()) {
            throw new IllegalArgumentException("The Package Name must not be empty");
        }
    }

    /**
     * Obtains the {@link String} representation of the {@link PackageName}.
     *
     * @return the {@link String} representation
     */
    public String get() {
        return this.packageName;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof final PackageName that)) {
            return false;
        }
        return Objects.equals(this.packageName, that.packageName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.packageName);
    }

    @Override
    public String toString() {
        return this.packageName;
    }

    /**
     * Creates a {@link PackageName} given a name.
     *
     * @param packageName the package name
     * @return a new {@link PackageName}
     */
    public static PackageName of(final String packageName) {
        return new PackageName(packageName);
    }
}
