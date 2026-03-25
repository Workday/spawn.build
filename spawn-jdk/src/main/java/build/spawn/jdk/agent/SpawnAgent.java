package build.spawn.jdk.agent;

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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;

/**
 * A Virtual Machine Agent for bootstrapping, isolating and starting the {@code SpawnAgent} {@code EmbeddedServer} in
 * its own {@link ClassLoader}.
 *
 * @author brian.oliver
 * @since Nov-2018
 */
public class SpawnAgent {

    /**
     * The name of the archive containing the {@link SpawnAgent}.
     */
    public static String ARCHIVE_NAME = "spawn-agent.jar";

    /**
     * The name of the {@link SpawnAgent} {@code EmbeddedServer} to start.
     * <p>
     * <strong>NOTE: </strong> This must not be a reference to the actual {@code EmbeddedServer} {@link Class},
     * but instead a {@link String} representing the fully-qualified-name of the {@link Class}.   Using the actual
     * {@link Class} will cause it to be loaded with the {@link ClassLoader} of this {@link Class} and thus prevent it
     * from being isolated as required.
     */
    private final static String EMBEDDED_SERVER_CLASSNAME = "build.spawn.jdk.agent.EmbeddedServer";

    /**
     * Prevent instantiation of {@link SpawnAgent}.
     */
    private SpawnAgent() {
    }

    /**
     * Parses the specified arguments {@link String} containing comma separated key=value pairs,
     * into an equivalent {@link Properties}.
     *
     * @param arguments the arguments {@link String}
     * @return the {@link Properties}
     */
    private static Properties parseArguments(final String arguments) {
        final Properties properties = new Properties();

        final String trimmedArguments = arguments == null ? "" : arguments.trim();

        if (!trimmedArguments.isEmpty()) {
            Arrays.stream(trimmedArguments.split(","))
                .map(s -> s.split("=", 2))
                .forEach(entry -> {
                    if (entry.length == 1) {
                        properties.put(entry[0], "");
                    }
                    else if (entry.length == 2) {
                        properties.put(entry[0], entry[1]);
                    }
                });
        }

        return properties;
    }

    /**
     * Dynamically creates the {@code EmbeddedServer}, in its own {@link ClassLoader}.
     *
     * @return a new {@code EmbeddedServer} as an {@link Object} (as it's in it's own {@link ClassLoader})
     */
    private static Object createEmbeddedServer() {

        // establish a custom ClassLoader in which to load and isolate the EmbeddedServer
        final CustomClassLoader classLoader = new CustomClassLoader();

        // determine the location of the SpawnAgent jar (from which to load resources)
        final String javaAgentPrefix = "-javaagent:";
        final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

        return runtimeMXBean.getInputArguments().stream()
            .filter(argument -> argument.startsWith(javaAgentPrefix) && argument.contains(SpawnAgent.ARCHIVE_NAME))
            .map(argument -> argument.substring(javaAgentPrefix.length()))
            .map(argument -> argument.substring(0, argument.indexOf("=")))
            .map(File::new)
            .findFirst()
            .map(file -> {
                try (JarFile jarFile = new JarFile(file)) {

                    // read the internal resources from the SpawnAgent jar
                    // so they can be resolved when by the custom ClassLoader
                    final String prefix = "internal/";
                    final String suffix = ".class";

                    jarFile.stream()
                        .filter(entry -> !entry.isDirectory())
                        .filter(entry -> entry.getName().startsWith(prefix))
                        .filter(entry -> entry.getName().endsWith(suffix))
                        .forEach(entry -> {
                            final String className = entry.getName()
                                .substring(prefix.length(), entry.getName().length() - suffix.length())
                                .replace(File.separator, ".");

                            final int size = (int) entry.getSize();
                            final byte[] buffer = new byte[size];

                            try (InputStream stream = jarFile.getInputStream(entry)) {

                                final int read = stream.read(buffer);
                                if (read == size) {
                                    classLoader.addResource(className, buffer);
                                }
                                else {
                                    throw new IOException("Only read " + read + " byte(s) for " + className);
                                }
                            }
                            catch (final IOException e) {
                                throw new RuntimeException("Failed to read " + className + " from " + file, e);
                            }
                        });

                    // use the internal ClassLoader to load and instantiate the EmbeddedServer
                    final Class<?> agentClass = classLoader.loadClass(EMBEDDED_SERVER_CLASSNAME);

                    return agentClass.getDeclaredConstructor().newInstance();
                }
                catch (final InstantiationException | IllegalAccessException
                             | NoSuchMethodException | InvocationTargetException ie) {
                    throw new RuntimeException("Failed to instantiate " + EMBEDDED_SERVER_CLASSNAME, ie);
                }
                catch (final ClassNotFoundException cnfe) {
                    throw new RuntimeException("Failed to locate " + EMBEDDED_SERVER_CLASSNAME, cnfe);
                }
                catch (final IOException e) {
                    throw new RuntimeException("Failed to read the " + SpawnAgent.ARCHIVE_NAME, e);
                }
            })
            .orElseThrow(() -> new RuntimeException("Failed to determine the location of the " +
                SpawnAgent.ARCHIVE_NAME));
    }

    /**
     * The {@link SpawnAgent} premain execution callback handler from the Java Virtual Machine.
     *
     * @param arguments       the agent arguments
     * @param instrumentation the {@link Instrumentation}
     */
    public static void premain(final String arguments,
                               final Instrumentation instrumentation) {

        // parse the agent arguments into Properties
        final Properties properties = parseArguments(arguments);

        // dynamically create and reflectively start the EmbeddedServer
        final Object embeddedServer = createEmbeddedServer();

        try {
            embeddedServer.getClass()
                .getMethod("start", Properties.class, Instrumentation.class)
                .invoke(embeddedServer, properties, instrumentation);
        }
        catch (final NoSuchMethodException e) {
            throw new RuntimeException(
                "Failed to locate the public void start(Properties, Instrumentation) method on the SpawnAgent EmbeddedServer",
                e);
        }
        catch (final IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(
                "Failed to invoked the public void start(Properties, Instrumentation) method on the SpawnAgent EmbeddedServer",
                e);
        }
    }

    /**
     * The {@link SpawnAgent} agentmain execution callback handler from the Java Virtual Machine.
     *
     * @param arguments       the agent arguments
     * @param instrumentation the {@link Instrumentation}
     */
    public static void agentmain(final String arguments, final Instrumentation instrumentation) {
        premain(arguments, instrumentation);
    }

    /**
     * A {@link ClassLoader} allowing isolation of {@link Class} and resource definitions.
     */
    public static class CustomClassLoader
        extends ClassLoader {

        /**
         * The resources loadable by the {@link CustomClassLoader}.
         */
        private final ConcurrentHashMap<String, byte[]> resources;

        /**
         * Constructs an {@link CustomClassLoader} with the default parent {@link ClassLoader}.
         */
        CustomClassLoader() {
            super();

            this.resources = new ConcurrentHashMap<>();
        }

        /**
         * Adds the specified named resource to the {@link CustomClassLoader}.
         *
         * @param name  the name of the resource
         * @param bytes the bytes for the resource
         */
        void addResource(final String name, final byte[] bytes) {
            this.resources.put(name, bytes);
        }

        @Override
        protected Class<?> findClass(final String name)
            throws ClassNotFoundException {

            final byte[] bytes = this.resources.get(name);

            if (bytes == null) {
                throw new ClassNotFoundException(name);
            }

            return defineClass(name, bytes, 0, bytes.length);
        }

        @Override
        public InputStream getResourceAsStream(final String name) {
            final byte[] bytes = this.resources.get(name);

            if (bytes == null) {
                return null;
            }

            return new ByteArrayInputStream(bytes);
        }
    }
}
