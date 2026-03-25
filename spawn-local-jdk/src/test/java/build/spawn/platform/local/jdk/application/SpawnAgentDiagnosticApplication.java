package build.spawn.platform.local.jdk.application;

/*-
 * #%L
 * Spawn Local JDK
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

/**
 * A simple application to test the availability of the {@code SpawnAgent}.
 *
 * @author brian.oliver
 * @since Nov-2019
 */
public class SpawnAgentDiagnosticApplication {

    /**
     * A private constructor to ensure no construction.
     */
    private SpawnAgentDiagnosticApplication() {
        // ensure no construction
    }

    /**
     * Application Entry Point.
     *
     * @param args the command line arguments
     */
    public static void main(final String[] args) {

        // attempt to obtain the isolated the SpawnAgent EmbeddedServer
        int result;
        try {
            ClassLoader.getSystemClassLoader()
                .loadClass("build.spawn.jdk.agent.EmbeddedServer");

            result = 1;
        }
        catch (final ClassNotFoundException e) {
            result = 42;
        }
        System.exit(result);
    }
}
