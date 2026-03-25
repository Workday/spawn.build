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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link InvocationHandler} which handles routing {@link Method} invocations between a {@link Faceted} instance and its
 * comprising {@link Facet}s.
 */
public class FacetedInvocationHandler
    implements Faceted, InvocationHandler {

    /**
     * The {@link Map} linking the interfaces that this {@link FacetedInvocationHandler} handles to the implementations
     * that it uses to fulfill requests against those interfaces.
     */
    private final LinkedHashMap<Class<?>, Object> interfaceToImplementationMap;

    /**
     * The {@link ConcurrentHashMap} used to lazily link interfaces which are superclasses of {@link Facet} interfaces
     * present in this {@link FacetedInvocationHandler} with the proper subclass implementations.
     */
    private final ConcurrentHashMap<Class<?>, Object> lazySuperclassToImplementationMap;

    /**
     * The {@link Proxy} {@link Object} which represents the {@link Faceted} instance and its {@link Facet}s handled by
     * this {@link FacetedInvocationHandler}.
     */
    private final Object proxy;

    /**
     * Creates a new {@link FacetedInvocationHandler} to handle {@link Method} invocations on the supplied
     * {@link Facet}s.
     *
     * @param context the {@link Context} in which the {@link Facet}s will be instantiated
     * @param facets  the {@link Facet}s to handle invocations
     */
    private FacetedInvocationHandler(final Context context, final Facet<?>... facets) {

        Objects.requireNonNull(facets);

        this.interfaceToImplementationMap = Arrays.stream(facets)
            .collect(Collectors.toMap(
                Facet::getInterface,
                facet -> facet.getFactory().apply(context),
                (iface, impl) -> {
                    throw new IllegalArgumentException(
                        "This Faceted instance already contains an implementation for "
                            + iface);
                },
                LinkedHashMap::new));

        this.lazySuperclassToImplementationMap = new ConcurrentHashMap<>();

        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final ArrayList<Class<?>> interfaces = new ArrayList<>();

        interfaces.add(Faceted.class);
        Stream.of(facets).map(Facet::getInterface).forEach(interfaces::add);

        final Class<?>[] classes = interfaces.toArray(new Class[0]);

        this.proxy = Proxy.newProxyInstance(classLoader, classes, this);
    }

    /**
     * Internal implementation of {@link Object#equals(Object)} to be used when comparing {@link Proxy} instances which
     * use the {@link FacetedInvocationHandler}.
     *
     * @param self  this {@link Proxy} instance
     * @param other the other {@link Proxy} instance
     * @return whether the {@link Proxy} for this {@link FacetedInvocationHandler} equals a different {@link Object}
     */
    private boolean equalsInternal(final Object self, final Object other) {

        if (other == null) {
            return false;
        }
        if (other.getClass() != self.getClass()) {
            return false;
        }
        final InvocationHandler handler = Proxy.getInvocationHandler(other);
        if (!(handler instanceof FacetedInvocationHandler)) {
            return false;
        }
        return handler.equals(this);
    }

    @Override
    public <T> Optional<T> as(final Class<T> facetClass) {
        return facetClass != null && facetClass.isInstance(this.proxy)
            ? Optional.of(facetClass.cast(this.proxy))
            : Optional.empty();
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args)
        throws Throwable {

        if (method.equals(Object.class.getMethod("equals", Object.class))) {
            return equalsInternal(proxy, args[0]);
        }

        if (method.equals(Object.class.getMethod("hashCode"))) {
            return hashCode();
        }

        final Class<?> declaringClass = method.getDeclaringClass();

        if (Faceted.class.equals(declaringClass)) {
            return method.invoke(this, args);
        }

        final Object target = this.interfaceToImplementationMap.get(declaringClass);

        if (target != null) {
            return method.invoke(target, args);
        }

        final Object adjustedTarget = this.lazySuperclassToImplementationMap.compute(declaringClass,
            (k, v) -> this.interfaceToImplementationMap.keySet().stream()
                .filter(k::isAssignableFrom)
                .findFirst()
                .map(this.interfaceToImplementationMap::get)
                .orElse(null));

        if (adjustedTarget == null) {
            throw new IllegalArgumentException("Did not contain Facet implementing " + declaringClass);
        }

        return method.invoke(adjustedTarget, args);
    }

    /**
     * Gets the {@link Proxy} {@link Object} representing the {@link Faceted} instance handled by this
     * {@link FacetedInvocationHandler}.
     *
     * @return the {@link Faceted} {@link Proxy} instance
     */
    private Faceted getProxy() {
        return (Faceted) this.proxy;
    }

    /**
     * Creates a new {@link Faceted} {@link Proxy} instance composed of the supplied {@link Facet}s.
     *
     * @param context the {@link Context} in which the {@link Facet}s will be created
     * @param facets  the {@link Facet}s to compose the {@link Faceted} instance
     * @return the {@link Faceted} proxy
     */
    static Faceted createProxy(final Context context, final Facet<?>... facets) {
        return new FacetedInvocationHandler(context, facets).getProxy();
    }
}
