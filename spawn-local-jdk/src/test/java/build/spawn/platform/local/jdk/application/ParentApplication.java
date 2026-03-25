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

import build.spawn.application.Console;
import build.spawn.application.option.Orphanable;
import build.spawn.jdk.JDKApplication;
import build.spawn.jdk.option.MainClass;
import build.spawn.jdk.option.SystemProperty;
import build.spawn.platform.local.LocalMachine;

/**
 * An application that is the "parent" of {@link ChildApplication}s.
 *
 * @author brian.oliver
 * @since Nov-2018
 */
public class ParentApplication {

    /**
     * A private constructor to ensure no construction.
     */
    private ParentApplication() {
        // ensure no construction
    }

    /**
     * Application Entry Point.
     *
     * @param args the command line arguments
     */
    public static void main(final String[] args) {

        final var machine = LocalMachine.get();

        // launch a child application
        try (var child = machine.launch(JDKApplication.class,
            MainClass.of(ChildApplication.class),
            SystemProperty.of("uri", System.getProperty("uri")),
            Orphanable.valueOf(System.getProperty("orphanable", Orphanable.DISABLED.name())),
            Console.ofSystem())) {

            // wait for the child application to terminate
            child.onExit()
                .join();
        }
    }
}
