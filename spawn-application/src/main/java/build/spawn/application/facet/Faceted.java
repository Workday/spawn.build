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

import build.codemodel.injection.Context;

import java.util.Optional;

/**
 * A runtime generated {@link Class} of {@link Object} that dynamically implements zero or more interfaces, often called
 * {@link Facet}s, wherein each interface is backed by its own corresponding implementation instance. All method
 * invocations on the interfaces implemented by a runtime generated {@link Object} are automatically delegated to the
 * corresponding interface implementation, thus allowing the said runtime generated {@link Object} to be
 * multi-{@link Faceted}.
 *
 * <p>
 * <p>
 * {@link Facet}s are unaware of each other and have no means of accessing one another. However, it is possible to cast
 * a runtime generated {@link Faceted} {@link Object} from one {@link Facet} type to another, so long as the
 * {@link Faceted} {@link Object} includes said interfaces.
 *
 * @author graeme.campbell
 * @since Jul-2019
 */
public interface Faceted {

    /**
     * Gets an {@link Optional} containing an implementation of the provided facetClass if this {@link Faceted} instance
     * has that class.
     *
     * @param facetClass the {@link Class} of facet to get from this {@link Faceted} instance
     * @param <T>        the type to return the {@link Facet} as
     * @return an {@link Optional} containing the backing {@link Facet} for {@link T} if present
     */
    <T> Optional<T> as(Class<T> facetClass);

    /**
     * Creates a new {@link Faceted} instance from the provided {@link Facet}s.
     *
     * @param context the {@link Context} in which the {@link Facet}s will be instantiated
     * @param facets  the {@link Facet}s to compose the {@link Faceted} instance from
     * @return a new {@link Faceted} instance
     */
    static Faceted create(final Context context, final Facet<?>... facets) {
        return FacetedInvocationHandler.createProxy(context, facets);
    }
}
