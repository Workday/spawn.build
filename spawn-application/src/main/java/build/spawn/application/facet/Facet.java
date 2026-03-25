package build.spawn.application.facet;

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

import build.base.configuration.CollectedOption;
import build.base.foundation.Preconditions;
import build.codemodel.injection.Context;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * An interface/implementation factory pairing which may be included in a {@link Faceted} instance.
 *
 * @author graeme.campbell
 * @since Jul-2019
 */
public interface Facet<T>
    extends CollectedOption<List> {

    /**
     * Obtains the type of interface for the {@link Facet}.
     *
     * @return the interface for this {@link Facet}
     */
    Class<T> getInterface();

    /**
     * Obtains the {@link Function} which creates the implementation for this {@link Facet}.
     *
     * @return an instance backing this {@link Facet}
     */
    Function<Context, ? extends T> getFactory();

    /**
     * Creates a new {@link Facet} from an interface/implementation pair.
     *
     * @param classOfFacet the class of the interface which the implementation implements
     * @param factory      a factory for an instance implementing the interface
     * @param <T>          the type of the implementation
     * @return a new {@link Facet}
     */
    static <T> Facet<T> of(final Class<T> classOfFacet,
                           final Function<Context, ? extends T> factory) {

        Objects.requireNonNull(classOfFacet, "The interface class for a Facet must be provided.");
        Objects.requireNonNull(factory, "An implementation factory for a Facet must be provided.");

        Preconditions.require(classOfFacet.isInterface(), true);

        return new Facet<T>() {

            @Override
            public Class<T> getInterface() {
                return classOfFacet;
            }

            @Override
            public Function<Context, ? extends T> getFactory() {
                return factory;
            }

            @Override
            public int hashCode() {
                return classOfFacet.hashCode();
            }

            @Override
            public boolean equals(final Object obj) {

                if (obj instanceof Facet) {
                    final Facet other = (Facet) obj;
                    return other.getInterface().equals(getInterface());
                }

                return false;
            }

            @Override
            public String toString() {
                return "Facet{Interface=" + getInterface().toString() + "}";
            }
        };
    }

    /**
     * Creates a new {@link Facet} from an interface.
     *
     * @param classOfFacet          the class of the interface which the implementation implements
     * @param classOfImplementation the class of the implementation this {@link Facet} should use
     * @param <T>                   the type of the implementation
     * @return a new {@link Facet}
     */
    static <T> Facet<T> of(final Class<T> classOfFacet,
                           final Class<? extends T> classOfImplementation) {

        Objects.requireNonNull(classOfFacet, "The interface class for a Facet must be provided.");
        Objects.requireNonNull(classOfImplementation, "The implementation class for a Facet must be provided.");

        Preconditions.require(classOfFacet.isInterface(), true);
        Preconditions.require(!classOfImplementation.isInterface(), true);
        Preconditions.require(classOfFacet.isAssignableFrom(classOfImplementation), true);

        return of(classOfFacet, context -> context.create(classOfImplementation));
    }
}
