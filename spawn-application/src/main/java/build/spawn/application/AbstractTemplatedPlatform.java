package build.spawn.application;

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

import build.base.configuration.Configuration;
import build.base.configuration.ConfigurationBuilder;
import build.base.expression.compat.Processor;
import build.base.expression.compat.ProcessorBuilder;
import build.base.expression.compat.Resolvable;
import build.base.expression.compat.Variable;
import build.base.foundation.Introspection;
import build.base.foundation.Preconditions;
import build.base.foundation.Strings;
import build.base.logging.Logger;
import build.base.naming.UniqueNameGenerator;
import build.base.network.Server;
import build.base.option.TemporaryDirectory;
import build.base.option.WorkingDirectory;
import build.base.table.Table;
import build.codemodel.foundation.naming.CachingNameProvider;
import build.codemodel.foundation.naming.NonCachingNameProvider;
import build.codemodel.injection.ConfigurationResolver;
import build.codemodel.injection.Context;
import build.codemodel.injection.InjectionException;
import build.codemodel.injection.InjectionFramework;
import build.codemodel.jdk.JDKCodeModel;
import build.spawn.application.option.LaunchIdentity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An abstract {@link TemplatedPlatform} that automatically detects the supported {@link Class}es of
 * {@link Application}s and associated {@link Launcher}s by loading and parsing properties files
 * (named using the concrete implementation of this {@link Class}) located in {@code META-INF/} folders on the
 * classpath.
 * <p>
 * The properties files define key-value pairs where each key is a fully-qualified-class-name for a {@link Class}
 * of {@link Application} and the corresponding value is the {@link Class} of {@link Launcher} to launch
 * said {@link Class}es of {@link Application} with the {@link Platform}.
 * <p>
 * For example, the {@code META-INF/build.spawn.local.LocalMachine} properties file for the
 * {@code LocalMachine} defines the following entries.
 * <code>
 * build.spawn.application.Application=build.spawn.local.LocalLauncher
 * build.spawn.application.java.JavaApplication=build.spawn.java.LocalJavaLauncher
 * </code>
 *
 * @author brian.oliver
 * @since Oct-2018
 */
public abstract class AbstractTemplatedPlatform
    implements TemplatedPlatform, AutoCloseable {

    /**
     * The {@link Logger} for the {@link Platform}.
     */
    private static final Logger LOGGER = Logger.get(AbstractTemplatedPlatform.class);

    /**
     * The name of the {@link Platform}.
     */
    private final String name;

    /**
     * The {@link Platform} {@link Configuration}.
     */
    private final Configuration platformConfiguration;

    /**
     * The {@link Launcher}s by {@link Class} of {@link Application} that are supported
     * for this {@link Platform}.
     */
    private final LinkedHashMap<Class<? extends Application>, Class<? extends Launcher>> launchers;

    /**
     * The {@link Server} that {@link Application}s may use to communicate with this {@link Platform}.
     */
    private final Server server;

    /**
     * The {@link AtomicLong} used to uniquely identify {@link Application}s launched on the same {@link Platform}.
     */
    private final AtomicLong nextLaunchIdentityNumber;

    /**
     * The {@link UniqueNameGenerator} for the {@link Platform}.
     */
    private final UniqueNameGenerator uniqueNameGenerator;

    /**
     * The {@link InjectionFramework} to be used by the {@link TemplatedPlatform}.
     */
    protected final InjectionFramework injectionFramework;

    /**
     * Constructs an {@link AbstractTemplatedPlatform}.
     *
     * @param name                  the name of the {@link Platform}
     * @param platformConfiguration the {@link Platform} {@link Configuration}
     */
    @SuppressWarnings("unchecked")
    public AbstractTemplatedPlatform(final String name,
                                     final Configuration platformConfiguration) {

        this.name = Strings.isEmpty(name) ? "anonymous" : Strings.trim(name);
        this.platformConfiguration = platformConfiguration == null
            ? Configuration.empty()
            : platformConfiguration;

        this.nextLaunchIdentityNumber = new AtomicLong(1);
        this.uniqueNameGenerator = new UniqueNameGenerator();

        // establish a codemodel and InjectionFramework for this platform
        final var nameProvider = new CachingNameProvider(new NonCachingNameProvider());
        final var codemodel = new JDKCodeModel(nameProvider);
        this.injectionFramework = new InjectionFramework(codemodel);

        // attempt to establish the server for this platform
        try {
            // create and start the Server
            this.server = new Server(0).start();
        } catch (final IOException e) {
            throw new RuntimeException("Failed to create " + getClass().getCanonicalName(), e);
        }

        // determine the launchers supported by this platform
        this.launchers = new LinkedHashMap<>();

        // attempt to load the properties file defining the launchers for this class of platform
        final String path = "META-INF/" + getClass().getName();

        final String platformClassName = getClass().getCanonicalName();
        LOGGER.debug("Locating Application Launchers for {} in {} (as System Resources)", platformClassName, path);

        try {
            // determine the properties files defining launchers for the concrete type of platform
            final List<URL> resources = Collections.list(ClassLoader.getSystemResources(path));

            LOGGER.debug("Located {} System Resource(s) defining Application Launchers", resources.size());

            // attempt to find the Launcher for the class of Application
            for (final URL resource : resources) {

                LOGGER.debug("Loading Application Launcher(s) from {}", resource);

                try (LineNumberReader reader = new LineNumberReader(
                    new BufferedReader(new InputStreamReader(resource.openStream())))) {

                    String line;
                    while ((line = reader.readLine()) != null) {

                        line = Strings.trim(line);

                        if (!line.startsWith("#") && !Strings.isEmpty(line)) {
                            final String[] parts = line.split("=");

                            // the optionally detected name of the class for launching the application
                            Optional<String> launcherClassName = Optional.empty();

                            // by default we assume the specified launcher is for all classes of Application
                            Class<? extends Application> applicationClass = Application.class;

                            if (parts.length == 1) {
                                // when there's only a single parameter, assume that is the launcher
                                launcherClassName = Optional.of(Strings.trim(parts[0]));
                            } else if (parts.length == 2) {
                                // when there's a "key=value", the key is the application and the value is the launcher
                                final String specifiedClassName = Strings.trim(parts[0]);

                                try {
                                    final Class<?> specifiedClass = Class.forName(specifiedClassName);

                                    if (Application.class.isAssignableFrom(specifiedClass)) {
                                        applicationClass = (Class<? extends Application>) specifiedClass;
                                        launcherClassName = Optional.of(Strings.trim(parts[1]));
                                    } else {
                                        LOGGER.warn("The specified class [{}] in [{}] at line [{}] is not an {}.",
                                            launcherClassName.get(), resource, reader.getLineNumber(),
                                            Application.class.getCanonicalName());
                                    }
                                } catch (final ClassNotFoundException e) {
                                    LOGGER.warn("The specified class [{}] in [{}] at line [{}] is not found.",
                                        specifiedClassName, resource, reader.getLineNumber());
                                }
                            } else {
                                // the file isn't formatted correctly!
                                LOGGER.warn(
                                    "Invalid property definition detected in [{}] for [{}] at line [{}].\nLine:{}",
                                    resource, platformClassName, reader.getLineNumber(), line);
                            }

                            if (launcherClassName.isPresent()) {
                                try {
                                    final Class<?> launcherClass = Class.forName(launcherClassName.get());

                                    if (Launcher.class.isAssignableFrom(launcherClass)) {

                                        LOGGER.trace("[{}] classes will use [{}] for launching on [{}]",
                                            applicationClass, launcherClass, platformClassName);

                                        // remember the launcher for this platform
                                        this.launchers.put(applicationClass,
                                            (Class<? extends Launcher>) launcherClass);
                                    } else {
                                        LOGGER.warn("The specified class [{}] in [{}] at line [{}] is not a {}",
                                            launcherClassName.get(), resource, reader.getLineNumber(),
                                            Launcher.class);
                                    }
                                } catch (final InjectionException e) {
                                    LOGGER.warn(
                                        "The specified class [{}] in [{}] at line [{}] could not be created. The"
                                            + " launcher will not be available", launcherClassName.get(), resource,
                                        reader.getLineNumber(), e);
                                } catch (final ClassNotFoundException e) {
                                    LOGGER.warn("The specified class [{}] in [{}] at line [{}] is not found.  The "
                                            + "launcher will not be available", launcherClassName.get(), resource,
                                        reader.getLineNumber(), e);
                                }
                            }
                        }
                    }
                }
            }
        } catch (final IOException e) {
            LOGGER.warn("Failed to determine System Resources for [{}]", path, e);
        }
    }

    @Override
    public InjectionFramework injectionFramework() {
        return this.injectionFramework;
    }

    @Override
    public void close() {
        this.server.close();
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public Configuration configuration() {
        return this.platformConfiguration;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Context createContext(final Class<? extends Application> applicationClass,
                                 final Configuration configuration) {

        Objects.requireNonNull(applicationClass, "The Application Class must not be null");
        Objects.requireNonNull(configuration, "The Configuration must not be null");

        // establish a Context with the OptionsByType as a Resolver
        final Context context = this.injectionFramework.newContext();

        // allow the Context to be injected
        context.bind(Context.class).to(context);

        // allow the InjectionFramework to be injected
        context.bind(InjectionFramework.class).to(this.injectionFramework);

        // allow all interfaces of the Platform to be injected
        Introspection.getAll(getClass(), Class::getInterfaces)
            .forEach(i -> context.bind((Class) i).to(this));

        // allows the Platform Server to be injected
        context.bind(Server.class).to(this.server);

        // allow the UniqueNameGenerator to be injected
        context.bind(UniqueNameGenerator.class).to(this.uniqueNameGenerator);

        // allow Configuration Options to be resolved (including Defaults)
        context.addResolver(ConfigurationResolver.of(configuration));

        // allow the class of application to be injected
        context.bind(Class.class).to(applicationClass);

        // allow the Configuration to be injected
        context.bind(Configuration.class).to(configuration);

        return context;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <A extends Application> A launch(final Specification<A> specification) {

        try {
            Objects.requireNonNull(specification, "The Application Specification must not be null");

            // ensure the Application.Configuration is compatible with this Platform
            if (!specification.isSupported(this)) {
                throw new IllegalArgumentException(
                    "The Application Specification is not supported by this platform");
            }

            // ensure the class of Application we'll be launching is an interface
            // (no concrete classes are allowed as we use the interface to obtain the required Launcher)
            final Class<? extends A> applicationClass = specification.getApplicationClass();

            Preconditions.require(applicationClass, applicationClass.isInterface(),
                "The specified class of Application is not an interface");

            // establish the initial Configuration Options that may need resolving with an Expression Processor
            // (commencing with the Platform Configuration and then adding the Specification Options to allow overriding)
            final var resolvableConfiguration = ConfigurationBuilder.create()
                .include(this.platformConfiguration)
                .include(specification.options())
                .build();

            // establish and prepare the Expression Processor
            final var processorBuilder = ProcessorBuilder.create();

            // include the Platform (and Machine) as variables
            processorBuilder.define(Variable.of("platform", this));
            if (this instanceof Machine) {
                processorBuilder.define(Variable.of("machine", this));
            }

            // include the TemporaryDirectory as the ${tmp} variable
            resolvableConfiguration.getOptionalValue(TemporaryDirectory.class)
                .ifPresent(path -> processorBuilder.define(Variable.of("tmp", path)));

            // include the WorkingDirectory as the ${home} variable
            resolvableConfiguration.getOptionalValue(WorkingDirectory.class)
                .ifPresent(path -> processorBuilder.define(Variable.of("home", path)));

            // include Resolvable Options in the Processor
            resolvableConfiguration.stream(Resolvable.class)
                .forEach(processorBuilder::include);

            final var processor = processorBuilder.build();

            // establish the launch ConfigurationBuilder by resolving the ResolvableOptions using the Processor
            final var launchConfigurationBuilder = ConfigurationBuilder.create();

            resolvableConfiguration.stream()
                .map(processor::resolve)
                .forEach(launchConfigurationBuilder::add);

            // include a new LaunchIdentity for this Application
            final var launchIdentity = LaunchIdentity.of(this.nextLaunchIdentityNumber.getAndIncrement());
            launchConfigurationBuilder.add(launchIdentity);

            // establish a Context with which to create the Application and Customizers
            final var context = createContext(applicationClass, launchConfigurationBuilder.build());

            // TODO: re-introduce when working with Java-based applications
            //            // include the Collider Agent Path
            //            context.bind(Path.class).to(this.colliderAgentPath);

            // establish the diagnostics Table
            final var diagnostics = Table.create();
            context.bind(Table.class).to(diagnostics);

            // include the Processor
            context.bind(Processor.class).to(processor);

            // include the Specification (but should we?)
            context.bind(Specification.class).to(specification);

            // discover "Customizer" Application Options by reflectively locating those that are directly defined
            // on Application interfaces, creating and adding them in reverse order to the launch options
            final var interfaces = Introspection
                .getAll(applicationClass, Class::getInterfaces)
                .collect(Collectors.toCollection(LinkedList::new));

            // include the Application interface itself in the discovery
            interfaces.addFirst(applicationClass);

            // resolve the Application.Customizers
            interfaces.stream()
                .filter(Application.class::isAssignableFrom)
                .flatMap(c -> Stream.of(c.getDeclaredClasses()))
                .filter(Customizer.class::isAssignableFrom)
                .filter(c -> c.getSimpleName().equals("Customizer"))
                .filter(c -> {
                    final int modifiers = c.getModifiers();
                    return !Modifier.isInterface(modifiers) &&
                        !Modifier.isAbstract(modifiers) &&
                        Modifier.isPublic(modifiers) &&
                        Modifier.isStatic(modifiers) &&
                        !c.isEnum() &&
                        !c.isAnnotation();
                })
                .map(c -> (Class<Customizer>) c)
                .collect(Collectors.toCollection(ArrayDeque::new))
                .stream()
                .map(context::create)
                .forEachOrdered(launchConfigurationBuilder::add);

            // determine the class of Launcher for the Application
            //
            // IMPORTANT: use min() to select the most specific (most derived) registered application class,
            // NOT findFirst(). The launchers map is a LinkedHashMap populated by iterating META-INF resource
            // files in classpath order. Using findFirst() meant that whichever jar appeared earlier on the
            // classpath determined the launcher — e.g. if spawn-local-platform appeared before spawn-local-jdk,
            // then Application→LocalLauncher would win over JDKApplication→LocalJDKLauncher for any
            // JDKApplication subclass. This caused silent, order-dependent failures that were extremely hard
            // to diagnose (the symptom was "Failed to provide or determine the Executable to launch" or
            // the JVM receiving program arguments as JVM flags). Using min() with class hierarchy ordering
            // ensures the most derived registered type always wins, regardless of classpath order.
            final Class<? extends Launcher> launcherClass = this.launchers.entrySet()
                .stream()
                .filter(e -> e.getKey().isAssignableFrom(applicationClass))
                .min((a, b) -> a.getKey().isAssignableFrom(b.getKey()) ? 1 : -1)
                .map(Map.Entry::getValue)
                .orElseThrow(
                    () -> new UnsupportedOperationException(
                        "Could not determine a suitable Launcher for the application class "
                            + applicationClass.getCanonicalName()));

            // establish the Application.Launcher
            final Launcher<A, Platform> launcher = context.create(launcherClass);

            // establish the launch Configuration
            final var launchConfiguration = launchConfigurationBuilder.build();

            return launcher.launch(this, applicationClass, launchConfiguration);
        } catch (final Throwable e) {
            LOGGER.error("Unable to launch application", e);
            throw new RuntimeException(e);
        }
    }
}
