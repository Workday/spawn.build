package build.spawn.application.composition;

/*-
 * #%L
 * Spawn Application Composition
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

import build.base.configuration.Configuration;
import build.base.configuration.ConfigurationBuilder;
import build.base.configuration.Option;
import build.base.foundation.stream.Streams;
import build.spawn.application.Application;
import build.spawn.application.Lifecycle;
import build.spawn.application.Machine;
import build.spawn.application.Platform;
import build.spawn.application.Specification;
import build.spawn.application.composition.option.ApplicationIdentifier;
import build.spawn.application.option.DiagnosticName;
import build.spawn.application.option.DiagnosticNameProvider;
import build.spawn.jdk.option.SystemProperty;
import static build.spawn.application.composition.option.ApplicationIdentifier.SYSTEM_PROPERTY;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * A collection of zero or more {@link Application}s that may be managed together.
 *
 * @author brian.oliver
 * @since Jun-2020
 */
public class Composition
    implements AutoCloseable, Lifecycle<ApplicationStream<Application>> {

    /**
     * The {@link Application}s currently in the {@link Composition}.
     */
    private final CopyOnWriteArrayList<Application> applications;

    /**
     * The next identifier for newly launched {@link Application}s.
     */
    private final AtomicInteger nextIdentifier;

    /**
     * Constructs a {@link Composition} given a {@link Stream} of {@link Application}s.
     *
     * @param applications the {@link Stream} of {@link Application}
     */
    public Composition(final Stream<? extends Application> applications) {
        this.applications = new CopyOnWriteArrayList<>();
        applications.forEach(this::add);

        this.nextIdentifier = new AtomicInteger(this.applications.size() + 1);
    }

    /**
     * Requests the {@link Application}s in the {@link Composition} be cleanly shutdown, by invoking
     * {@link Application#shutdown()} for each {@link Application}.
     * <p>
     * Once this method returns, all {@link Application}s in the {@link Composition} can be assumed to have been
     * terminated and are no longer part of the {@link Composition}.
     *
     * @return an {@link build.spawn.application.composition.ApplicationStream} of the {@link Application}s that were shutdown
     */
    public build.spawn.application.composition.ApplicationStream<Application> shutdown() {
        return stream().shutdown();
    }

    /**
     * Requests the {@link Application} in the {@link Composition} be forcibly terminated, by invoking
     * {@link Application#destroy()} for each {@link Application}.
     * <p>
     * Once this method returns, all {@link Application}s in the {@link Composition} can be assumed to have been
     * terminated and are no longer part of the {@link Composition}.
     *
     * @return an {@link build.spawn.application.composition.ApplicationStream} of the {@link Application}s that were destroyed
     */
    public build.spawn.application.composition.ApplicationStream<Application> destroy() {

        final Stream<Application> reversed = Streams.reverse(stream());
        reversed.forEach(Application::destroy);

        return new ApplicationStream<>(reversed);
    }

    /**
     * Adds an {@link Application} to the {@link Composition}.
     *
     * @param application the {@link Application}
     */
    void add(final Application application) {
        this.applications.add(application);

        // when the application exits, remove it from the deployment
        application.onExit().thenAccept(x -> this.applications.remove(application));
    }

    /**
     * Obtains the number of {@link Application}s in the {@link Composition}.
     *
     * @return the number of {@link Application}s
     */
    public int size() {
        return this.applications.size();
    }

    /**
     * Determines if {@link Composition} is empty (contains no {@link Application}s).
     *
     * @return {@code true} if there are no {@link Application}s in the {@link Composition}, {@code false} otherwise
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Obtains an {@link build.spawn.application.composition.ApplicationStream} containing the {@link Application}s in the {@link Composition}.
     *
     * @return an {@link build.spawn.application.composition.ApplicationStream}
     */
    public build.spawn.application.composition.ApplicationStream<Application> stream() {
        return new ApplicationStream<>(this.applications.stream());
    }

    /**
     * Obtains an {@link build.spawn.application.composition.ApplicationStream} containing the {@link Application}s of the specified {@link Class} in
     * the {@link Composition}.
     *
     * @param <A>              the type of the {@link Application}s
     * @param applicationClass the {@link Class} of the {@link Application}
     * @return an {@link build.spawn.application.composition.ApplicationStream}
     */
    public <A extends Application> build.spawn.application.composition.ApplicationStream<A> stream(final Class<A> applicationClass) {
        return new ApplicationStream<>(this.applications.stream()).filter(applicationClass);
    }

    /**
     * Requests the {@link Application}s in the {@link Composition} be suspended with {@link Application#suspend()}.
     *
     * @return a {@link CompletableFuture} containing a {@link build.spawn.application.composition.ApplicationStream} of the {@link Application}s
     * on which {@link Application#suspend()} was invoked
     * @see Application#suspend()
     */
    public CompletableFuture<build.spawn.application.composition.ApplicationStream<Application>> suspend() {
        return stream().suspend();
    }

    /**
     * Requests the {@link Application}s in the {@link Composition} be resumed with {@link Application#resume()}.
     *
     * @return a {@link CompletableFuture} containing a {@link build.spawn.application.composition.ApplicationStream} of the {@link Application}s
     * on which {@link Application#resume()} was invoked
     * @see Application#suspend()
     */
    public CompletableFuture<build.spawn.application.composition.ApplicationStream<Application>> resume() {
        return stream().resume();
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<build.spawn.application.composition.ApplicationStream<Application>> onStart() {
        return (CompletableFuture<build.spawn.application.composition.ApplicationStream<Application>>) stream().onStart();
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompletableFuture<build.spawn.application.composition.ApplicationStream<Application>> onExit() {
        return (CompletableFuture<build.spawn.application.composition.ApplicationStream<Application>>) stream().onExit();
    }

    /**
     * Requests the {@link Application}s in the {@link Composition} be forcibly terminated.
     * <p>
     * This is semantically equivalent to invoking {@link #destroy()}.
     */
    @Override
    public void close() {
        destroy();
    }

    /**
     * An {@link build.spawn.application.composition.ApplicationStream} for a {@link Composition}.
     */
    private class ApplicationStream<A extends Application>
        implements build.spawn.application.composition.ApplicationStream<A> {

        /**
         * The underlying {@link Stream} of {@link Application}s to process.
         */
        private final Stream<A> applications;

        /**
         * Constructs an {@link ApplicationStream} given a {@link Stream} of {@link Application}s.
         *
         * @param applications the {@link Stream} of {@link Application}s
         */
        private ApplicationStream(final Stream<A> applications) {
            this.applications = applications;
        }

        @Override
        @SuppressWarnings("unchecked")
        public CompletableFuture<build.spawn.application.composition.ApplicationStream<A>> suspend() {
            // collect the alive applications to suspend
            final List<A> applications = stream()
                .map(application -> (A) application)
                .filter(Application::isAlive)
                .toList();

            // suspend the applications
            return CompletableFuture.allOf(applications.stream()
                    .map(Application::suspend)
                    .toArray(CompletableFuture[]::new))
                .thenApply(__ -> new ApplicationStream<>(applications.stream()));
        }

        @Override
        @SuppressWarnings("unchecked")
        public CompletableFuture<build.spawn.application.composition.ApplicationStream<A>> resume() {
            // collect the alive applications to resume
            final List<A> applications = stream()
                .map(application -> (A) application)
                .filter(Application::isAlive)
                .collect(Collectors.toCollection(ArrayList::new));

            // resume the applications
            return CompletableFuture.allOf(applications.stream()
                    .map(Application::resume)
                    .toArray(CompletableFuture[]::new))
                .thenApply(__ -> new ApplicationStream<>(applications.stream()));
        }

        @Override
        public build.spawn.application.composition.ApplicationStream<A> shutdown() {
            // the applications that were terminated
            final ArrayList<A> applications = new ArrayList<>();

            // shutdown each application
            this.applications.forEach(application -> {
                Composition.this.applications.remove(application);
                application.shutdown();
                applications.add(application);
            });

            return new ApplicationStream<>(applications.stream());
        }

        @Override
        public build.spawn.application.composition.ApplicationStream<A> destroy() {
            // the applications that were terminated
            final ArrayList<A> applications = new ArrayList<>();

            // destroy each application
            this.applications.forEach(application -> {
                Composition.this.applications.remove(application);
                application.destroy();
                applications.add(application);
            });

            return new ApplicationStream<>(applications.stream());
        }

        @Override
        public build.spawn.application.composition.ApplicationStream<A> filter(final Predicate<? super A> predicate) {
            return new ApplicationStream<>(this.applications.filter(predicate));
        }

        @Override
        public <T extends A> build.spawn.application.composition.ApplicationStream<T> filter(final Class<T> applicationClass) {
            return new ApplicationStream<>(filter(applicationClass::isInstance).map(applicationClass::cast));
        }

        @Override
        public build.spawn.application.composition.ApplicationStream<A> relaunch(final Option... options) {
            return shutdown().clone(options);
        }

        @Override
        @SuppressWarnings("unchecked")
        public build.spawn.application.composition.ApplicationStream<A> clone(final int count,
                                                                              final Option... options) {

            final List<A> clones = flatMap(application -> {
                final List<A> applications = new ArrayList<>();

                for (int i = 0; i < Math.max(0, count); i++) {
                    try {
                        // establish the launch options
                        final var launchOptions = ConfigurationBuilder.create()
                            .include(application.configuration());

                        // TODO: repair when enabling JavaDebugging
                        //launchOptions.remove(JavaAgent.class);

                        // use a new application identifier for the clone
                        final int applicationId = Composition.this.nextIdentifier.getAndIncrement();
                        launchOptions.add(ApplicationIdentifier.of(applicationId));
                        launchOptions.add(SystemProperty.of(SYSTEM_PROPERTY, applicationId));

                        // TODO: repair when we have worked out Specification and .Custom
                        //
                        //                        // override the launch options with those specified
                        //                        launchOptions.include(options);
                        //                        final Specification<?> specification = application.getConfiguration();
                        //                        // if a configuration is passed in, use that configuration
                        //                        final A clone = (specification != null &&
                        //                            !specification.getClass().equals(Specification.Custom.class))
                        //                            ? (A) application.platform().launch(specification)
                        //                            : (A) application.platform().launch(application.getInterfaceClass(), launchOptions);
                        //
                        //                        // include the clone in the composition
                        //                        add(clone);
                        //
                        //                        // include the clone in the resulting stream
                        //                        applications.add(clone);
                    }
                    catch (final Throwable t) {
                        // TODO: LOG the failure
                    }
                }

                return applications.stream();
            }).toList();

            return new ApplicationStream<>(clones.stream());
        }

        @Override
        public CompletableFuture<build.spawn.application.composition.ApplicationStream<A>> onStart() {
            // take a copy of the current applications, as new ones may be added while we're starting
            final List<A> applications = this.applications.collect(Collectors.toList());

            return CompletableFuture.allOf(applications.stream()
                    .map(Application::onStart)
                    .toArray(CompletableFuture[]::new))
                .thenApply(__ -> new ApplicationStream<>(applications.stream()));
        }

        @Override
        public CompletableFuture<build.spawn.application.composition.ApplicationStream<A>> onExit() {
            // take a copy of the current applications, as new ones may be added while we're terminating
            final List<A> applications = this.applications.toList();

            return CompletableFuture.allOf(applications.stream()
                    .map(Application::onExit)
                    .toArray(CompletableFuture[]::new))
                .thenApply(__ -> new ApplicationStream<>(applications.stream()));
        }

        @Override
        public <R> Stream<R> map(final Function<? super A, ? extends R> mapper) {
            return this.applications.map(mapper);
        }

        @Override
        public IntStream mapToInt(final ToIntFunction<? super A> mapper) {
            return this.applications.mapToInt(mapper);
        }

        @Override
        public LongStream mapToLong(final ToLongFunction<? super A> mapper) {
            return this.applications.mapToLong(mapper);
        }

        @Override
        public DoubleStream mapToDouble(final ToDoubleFunction<? super A> mapper) {
            return this.applications.mapToDouble(mapper);
        }

        @Override
        public <R> Stream<R> flatMap(final Function<? super A, ? extends Stream<? extends R>> mapper) {
            return this.applications.flatMap(mapper);
        }

        @Override
        public IntStream flatMapToInt(final Function<? super A, ? extends IntStream> mapper) {
            return this.applications.flatMapToInt(mapper);
        }

        @Override
        public LongStream flatMapToLong(final Function<? super A, ? extends LongStream> mapper) {
            return this.applications.flatMapToLong(mapper);
        }

        @Override
        public DoubleStream flatMapToDouble(final Function<? super A, ? extends DoubleStream> mapper) {
            return this.applications.flatMapToDouble(mapper);
        }

        @Override
        public build.spawn.application.composition.ApplicationStream<A> distinct() {
            return new ApplicationStream<>(this.applications.distinct());
        }

        @Override
        public build.spawn.application.composition.ApplicationStream<A> sorted() {
            return new ApplicationStream<>(this.applications.sorted());
        }

        @Override
        public build.spawn.application.composition.ApplicationStream<A> sorted(final Comparator<? super A> comparator) {
            return new ApplicationStream<>(this.applications.sorted(comparator));
        }

        @Override
        public build.spawn.application.composition.ApplicationStream<A> peek(final Consumer<? super A> action) {
            return new ApplicationStream<>(this.applications.peek(action));
        }

        @Override
        public build.spawn.application.composition.ApplicationStream<A> limit(final long maxSize) {
            return new ApplicationStream<>(this.applications.limit(maxSize));
        }

        @Override
        public build.spawn.application.composition.ApplicationStream<A> skip(final long n) {
            return new ApplicationStream<>(this.applications.skip(n));
        }

        @Override
        public void forEach(final Consumer<? super A> action) {
            this.applications.forEach(action);
        }

        @Override
        public void forEachOrdered(final Consumer<? super A> action) {
            this.applications.forEachOrdered(action);
        }

        @Override
        public Object[] toArray() {
            return this.applications.toArray();
        }

        @Override
        public <T> T[] toArray(final IntFunction<T[]> generator) {
            return this.applications.toArray(generator);
        }

        @Override
        public A reduce(final A application, final BinaryOperator<A> accumulator) {
            return this.applications.reduce(application, accumulator);
        }

        @Override
        public Optional<A> reduce(final BinaryOperator<A> accumulator) {
            return this.applications.reduce(accumulator);
        }

        @Override
        public <U> U reduce(final U application,
                            final BiFunction<U, ? super A, U> accumulator,
                            final BinaryOperator<U> combiner) {
            return this.applications.reduce(application, accumulator, combiner);
        }

        @Override
        public <R> R collect(final Supplier<R> supplier,
                             final BiConsumer<R, ? super A> accumulator,
                             final BiConsumer<R, R> combiner) {
            return this.applications.collect(supplier, accumulator, combiner);
        }

        @Override
        public <R, T> R collect(final Collector<? super A, T, R> collector) {
            return this.applications.collect(collector);
        }

        @Override
        public Optional<A> min(final Comparator<? super A> comparator) {
            return this.applications.min(comparator);
        }

        @Override
        public Optional<A> max(final Comparator<? super A> comparator) {
            return this.applications.max(comparator);
        }

        @Override
        public long count() {
            return this.applications.count();
        }

        @Override
        public boolean anyMatch(final Predicate<? super A> predicate) {
            return this.applications.anyMatch(predicate);
        }

        @Override
        public boolean allMatch(final Predicate<? super A> predicate) {
            return this.applications.allMatch(predicate);
        }

        @Override
        public boolean noneMatch(final Predicate<? super A> predicate) {
            return this.applications.noneMatch(predicate);
        }

        @Override
        public Optional<A> findFirst() {
            return this.applications.findFirst();
        }

        @Override
        public Optional<A> findAny() {
            return this.applications.findAny();
        }

        @Override
        public Iterator<A> iterator() {
            return this.applications.iterator();
        }

        @Override
        public Spliterator<A> spliterator() {
            return this.applications.spliterator();
        }

        @Override
        public boolean isParallel() {
            return this.applications.isParallel();
        }

        @Override
        public build.spawn.application.composition.ApplicationStream<A> sequential() {
            return new ApplicationStream<>(this.applications.sequential());
        }

        @Override
        public build.spawn.application.composition.ApplicationStream<A> parallel() {
            return new ApplicationStream<>(this.applications.parallel());
        }

        @Override
        public build.spawn.application.composition.ApplicationStream<A> unordered() {
            return new ApplicationStream<>(this.applications.unordered());
        }

        @Override
        public build.spawn.application.composition.ApplicationStream<A> onClose(final Runnable closeHandler) {
            this.applications.onClose(closeHandler);
            return this;
        }

        @Override
        public void close() {
            this.applications.close();
        }
    }

    /**
     * A {@link Builder} for {@link Composition}s.
     */
    public static class Builder {

        /**
         * The default {@link Platform} to use for launching {@link Application}s which haven't specified
         * a {@link Platform}.
         */
        private Platform platform;

        /**
         * The default {@link ConfigurationBuilder} to be used for each launched {@link Application}.
         */
        private final ConfigurationBuilder options;

        /**
         * The {@link Composable}s to launch when building the {@link Composition}.
         */
        private final LinkedHashSet<Plan<?>> composables;

        /**
         * Constructs a {@link Builder}.
         */
        private Builder() {
            this.platform = null; //TODO: LocalMachine.get();
            this.options = ConfigurationBuilder.create();
            this.composables = new LinkedHashSet<>();

            // include the DiagnosticNameProvider for the Composition that produces DiagnosticNames
            // that include the ApplicationIdentifier
            this.options.addIfNotPresent(DiagnosticNameProvider.class,
                DiagnosticNameProvider.of((builder, name) -> {
                    final var identifier = builder.get(ApplicationIdentifier.class);
                    return DiagnosticName.of(String.format("%s-%d", name.get(), identifier.get()));
                }));
        }

        /**
         * Sets the default {@link Platform} the {@link Builder} will use for launching {@link Application}s.
         *
         * @param platform the {@link Platform}
         * @return this {@link Builder} to permit fluent-style method invocation
         */
        public Builder using(final Platform platform) {
            this.platform = platform;

            return this;
        }

        /**
         * Obtains the default {@link ConfigurationBuilder} to be used to when launching {@link Application}s with
         * this {@link Builder}.
         *
         * @return the {@link ConfigurationBuilder} for launching {@link Application}s
         */
        public ConfigurationBuilder options() {
            return this.options;
        }

        /**
         * Defines the default {@link Option}s to use when launching {@link Application}s with this {@link Builder}.
         *
         * @param options the {@link Option}s
         * @return the {@link Builder} to allow fluent-style method chaining
         */
        public Builder with(final Option... options) {
            return with(ConfigurationBuilder.create(options));
        }

        /**
         * Defines the default {@link Option}s to use when launching {@link Application}s with this {@link Builder}.
         *
         * @param options the {@link ConfigurationBuilder}s
         * @return the {@link Builder} to allow fluent-style method chaining
         */
        public Builder with(final ConfigurationBuilder options) {
            this.options.include(options);
            return this;
        }

        /**
         * Creates and adds a {@link Composable} for launching a single instance of the specified
         * {@link Class} of {@link Application} using the default {@link Builder} {@link Machine}, the count,
         * {@link Option}s and {@link Machine} may be overridden for the {@link Composable} using the provided
         * {@link Composable} methods.
         *
         * @param <A>              the type of {@link Application}
         * @param applicationClass the {@link Class} of {@link Application}
         * @return the {@link Composable}
         */
        public <A extends Application> Composable<A> add(final Class<? extends A> applicationClass) {
            Objects.requireNonNull(applicationClass, "The class of Application must not be null");

            final Plan<A> plan = new Plan<>(this, applicationClass);
            this.composables.add(plan);

            return plan;
        }

        /**
         * Creates and adds a {@link Composable} for launching a single instance of the specified {@link Class} of
         * {@link Application} using the default {@link Builder} {@link Machine} and a {@link Specification}.
         *
         * @param specification the {@link Specification}
         * @param <A>           the type of {@link Application}
         * @return the {@link Composable}
         */
        public <A extends Application> Composable<A> add(final Specification<A> specification) {
            Objects.requireNonNull(specification, "The configuration must not be null");

            final Plan<A> plan = new Plan<>(this, specification);
            this.composables.add(plan);

            return plan;
        }

        /**
         * Builds a {@link Composition} by launching the {@link Composable} {@link Application}s.
         * <p>
         * Should any one of the {@link Application}s fail to launch, all previously launched {@link Application}s will
         * be destroyed.
         *
         * @return a new {@link Composition}
         */
        public Composition build() {

            // the successfully launched Applications for the Composition
            final ConcurrentLinkedQueue<Application> applications = new ConcurrentLinkedQueue<>();

            // each launched Application has a unique ApplicationIdentifier with in the created Composition
            final AtomicInteger compositionId = new AtomicInteger(0);

            try {
                // launch the Composable Applications in the order of their required Composables
                final HashSet<Plan<?>> remaining = new HashSet<>(this.composables);
                final HashMap<Plan<?>, ArrayList<Application>> launched = new HashMap<>();

                while (!remaining.isEmpty()) {
                    remaining.stream()
                        .filter(composable -> launched.keySet().containsAll(composable.requires.keySet()))
                        .forEach(composable -> {
                            // ensure the required Applications satisfy their respective constraints
                            final CompletableFuture<?> future = CompletableFuture.allOf(
                                composable.requires.entrySet().stream()
                                    .flatMap(entry -> {
                                        final Plan<?> required = entry.getKey();
                                        final Function<? super Application, CompletableFuture<?>> constraint = entry.getValue();

                                        return launched.get(required).stream().map(constraint::apply);
                                    })
                                    .toArray(CompletableFuture[]::new));

                            // ensure the constraint is satisfied
                            try {
                                future.get();
                            }
                            catch (final Exception e) {
                                throw new RuntimeException("Failed to ensure Application constraints", e);
                            }

                            // launch the Applications defined by the Composable
                            final ConfigurationBuilder launchOptions = ConfigurationBuilder.create();
                            launchOptions.include(this.options);
                            launchOptions.include(composable.options);

                            for (int i = 0; i < composable.count; i++) {
                                // create an identifier for the application in the deployment
                                final int applicationId = compositionId.incrementAndGet();

                                // include an identifier for the deployment
                                launchOptions.add(ApplicationIdentifier.of(applicationId));
                                launchOptions.add(SystemProperty.of(SYSTEM_PROPERTY, applicationId));

                                // determine the Platform to use (either the default or the composable one)
                                final Platform platform = composable.platform == null
                                    ? this.platform
                                    : composable.platform;

                                // launch the application
                                final Application application = composable.specification == null ?
                                    platform.launch(composable.applicationClass, launchOptions) :
                                    platform.launch(composable.specification);

                                // remember the launched Application for the Composition
                                applications.add(application);

                                // remember the launched Application for the Composable
                                launched.compute(composable, (__, existing) -> {
                                    final ArrayList<Application> list = existing == null ? new ArrayList<>() : existing;
                                    list.add(application);
                                    return list;
                                });
                            }
                        });

                    // remove the launched from those remaining
                    remaining.removeAll(launched.keySet());
                }
            }
            catch (final Exception e) {
                // destroy the applications already created (no orphans to be left behind)
                applications.forEach(Application::destroy);

                // re-throw the exception
                throw e;
            }

            return new Composition(applications.stream());
        }

        /**
         * Creates a {@link Builder} using the default {@link Platform} on which to launch {@link Application}s.
         *
         * @return a new {@link Builder}
         */
        public static Builder create() {
            return new Builder();
        }

        /**
         * An internal implementation of a {@link Composable} for a specific {@link Builder}.
         *
         * @param <A> the type of {@link Application}
         */
        private static class Plan<A extends Application>
            implements Composable<A> {

            /**
             * The {@link Builder} that created the {@link Composable}.
             */
            private final Builder builder;

            /**
             * The {@link Class} of {@link Application}.
             */
            private final Class<? extends A> applicationClass;

            /**
             * Optional configuration class for this {@link Composable}.
             */
            private Specification<A> specification;

            /**
             * The {@link ConfigurationBuilder} for the {@link Composable}.
             */
            private final ConfigurationBuilder options;

            /**
             * The {@link Composable}s required by this {@link Composable}, together with their constraints.
             */
            private final LinkedHashMap<Plan<?>, Function<? super Application, CompletableFuture<?>>> requires;

            /**
             * The {@link Platform} on which to launch the {@link Application}s, when {@code null} means use the
             * default {@link Platform} specified by the {@link Composition} {@link Builder}.
             */
            private Platform platform;

            /**
             * The number of {@link Application}s to launch.
             */
            private int count;

            /**
             * Constructs a {@link Composable} for the specified {@link Class} of {@link Application}.
             *
             * @param builder          the {@link Builder} that created the {@link Composable}
             * @param applicationClass the {@link Class} of {@link Application}
             */
            private Plan(final Builder builder,
                         final Class<? extends A> applicationClass) {

                this.builder = builder;
                this.applicationClass = applicationClass;
                this.options = ConfigurationBuilder.create();
                this.requires = new LinkedHashMap<>();
                this.platform = null;
                this.count = 1;
            }

            /**
             * Constructs a {@link Composable} for the specified {@link Class} of {@link Application} with a
             * {@link Specification}.
             *
             * @param builder       the {@link Builder} that created the {@link Composable}
             * @param specification the {@link Specification}
             */
            private Plan(final Builder builder,
                         final Specification<A> specification) {
                this(builder, specification.getApplicationClass());
                this.specification = specification;
            }

            @Override
            public Class<? extends A> getApplicationClass() {
                return this.applicationClass;
            }

            @Override
            public ConfigurationBuilder options() {
                return this.options;
            }

            @Override
            public Composable<A> with(final Option... options) {
                this.options.include(options);
                return this;
            }

            @Override
            public Composable<A> with(final Specification<A> specification) {
                this.options.include(specification.options());
                return this;
            }

            @Override
            public Composable<A> with(final Configuration configuration) {
                this.options.include(configuration);
                return this;
            }

            @Override
            public Composable<A> launch(final int count) {
                if (count > 0) {
                    this.count = count;
                }
                return this;
            }

            @Override
            public Composable<A> using(final Platform platform) {
                if (platform != null) {
                    this.platform = platform;
                }
                return this;
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T extends Application> Composable<A> require(final Composable<T> composable,
                                                                 final Function<? super T, CompletableFuture<?>> constraint) {

                if (composable instanceof Plan
                    && ((Plan<?>) composable).builder == this.builder) {

                    // ensure we're not attempting to require ourselves

                    final Deque<Plan<?>> remaining = new LinkedList<>();
                    remaining.offer((Plan<?>) composable);

                    final HashSet<Plan<?>> processed = new HashSet<>();

                    while (!remaining.isEmpty()) {
                        final Plan<?> current = remaining.poll();

                        if (current == this) {
                            throw new IllegalArgumentException("Cyclic Composition Definition Detected");
                        }

                        // don't process this plan again
                        processed.add(current);

                        // include the required Composables of the current Composable
                        current.requires.keySet().stream()
                            .filter(plan -> !processed.contains(plan))
                            .forEach(remaining::offer);
                    }

                    this.requires.put((Plan<?>) composable,
                        (Function<? super Application, CompletableFuture<?>>) constraint);
                }
                else {
                    throw new IllegalArgumentException(
                        "The provided Composable is not from the same Composition.Builder");
                }

                return this;
            }
        }
    }

}
