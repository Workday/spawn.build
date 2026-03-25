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

import build.base.network.Client;

import java.net.URI;

/**
 * An application that is the "child" of a {@link ParentApplication}s.
 *
 * @author brian.oliver
 * @since Nov-2018
 */
public class ChildApplication {

    /**
     * A private constructor to ensure no construction.
     */
    private ChildApplication() {
        // ensure no construction
    }

    /**
     * Application Entry Point.
     *
     * @param args the command line arguments
     */
    public static void main(final String[] args)
        throws Exception {

        System.out.println("Welcome from the ChildApplication");

        // determine the URI to which to connect (so this child application)
        final URI uri = new URI(System.getProperty("uri"));

        System.out.println("Connecting to: " + uri);

        // connect to the collider.uri
        try (var client = new Client(42, uri)) {
            System.out.println("Connected to: " + uri);

            System.out.println("Waiting for the connection to be closed");

            // wait for the client to be disconnected
            client.onStopped().get();

            System.out.println("Connection terminated.");

            System.out.println("Goodbye from the ChildApplication");
        }
    }
}
