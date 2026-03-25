package build.spawn.jdk;

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

import build.spawn.application.AbstractSpecification;
import build.spawn.application.Application;
import build.spawn.application.Specification;
import build.spawn.jdk.option.ClassPath;
import build.spawn.jdk.option.MainClass;
import build.spawn.jdk.option.SystemProperty;

import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * An abstract {@link Application} {@link Specification} for {@link JDKApplication}s.
 *
 * @param <A> the type of {@link JDKApplication}
 * @param <S> the type of {@link AbstractJDKSpecification}
 * @author brian.oliver
 * @since Aug-2021
 */
public abstract class AbstractJDKSpecification<A extends JDKApplication, S extends AbstractJDKSpecification<A, S>>
    extends AbstractSpecification<A, S> {

    /**
     * Specifies the {@link MainClass} for the {@link JDKApplication}.
     *
     * @param mainClass the {@link MainClass}
     * @return this {@link Specification} to permit fluent-style method invocation
     */
    @SuppressWarnings("unchecked")
    public S withMainClass(final MainClass mainClass) {
        options().add(mainClass);
        return (S) this;
    }

    /**
     * Specifies the {@link Class} defining a main method for the {@link JDKApplication}.
     *
     * @param mainClass the {@link Class} definine a main method
     * @return this {@link Specification} to permit fluent-style method invocation
     */
    @SuppressWarnings("unchecked")
    public S withMainClass(final Class<?> mainClass) {
        options().add(MainClass.of(mainClass));
        return (S) this;
    }

    /**
     * Specifies the {@link ClassPath} for the {@link JDKApplication}.
     *
     * @param classPath the {@link ClassPath}
     * @return this {@link Specification} to permit fluent-style method invocation
     */
    @SuppressWarnings("unchecked")
    public S withClassPath(final ClassPath classPath) {
        options().add(classPath);
        return (S) this;
    }

    /**
     * Specifies the {@link ClassPath} for the {@link JDKApplication}.
     *
     * @param paths the {@link Path}s to be included in the {@link ClassPath}
     * @return this {@link Specification} to permit fluent-style method invocation
     */
    @SuppressWarnings("unchecked")
    public S withClassPath(final Path... paths) {
        options().add(ClassPath.of(paths));
        return (S) this;
    }

    /**
     * Specifies the {@link ClassPath} for the {@link JDKApplication}.
     *
     * @param paths the {@link Path}s to be included in the {@link ClassPath}
     * @return this {@link Specification} to permit fluent-style method invocation
     */
    @SuppressWarnings("unchecked")
    public S withClassPath(final Stream<Path> paths) {
        options().add(ClassPath.of(paths));
        return (S) this;
    }

    /**
     * Specifies a {@link SystemProperty} for the {@link JDKApplication}.
     *
     * @param key the key
     * @return this {@link Specification} to permit fluent-style method invocation
     */
    @SuppressWarnings("unchecked")
    public S withSystemProperty(final String key) {
        options().add(SystemProperty.of(key));
        return (S) this;
    }

    /**
     * Specifies a {@link SystemProperty} for the {@link JDKApplication}.
     *
     * @param systemProperty the {@link SystemProperty}
     * @return this {@link Specification} to permit fluent-style method invocation
     */
    @SuppressWarnings("unchecked")
    public S withSystemProperty(final SystemProperty systemProperty) {
        options().add(systemProperty);
        return (S) this;
    }

    /**
     * Specifies a {@link SystemProperty} for the {@link JDKApplication}.
     *
     * @param key   the key
     * @param value the value
     * @return this {@link Specification} to permit fluent-style method invocation
     */
    @SuppressWarnings("unchecked")
    public S withSystemProperty(final String key, final Object value) {
        options().add(SystemProperty.of(key, value));
        return (S) this;
    }
}
