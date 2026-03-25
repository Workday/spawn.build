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

import java.time.Duration;

/**
 * An application that sleeps for a specified duration before terminating.
 * 
 * @author brian.oliver 
 * @since Oct-2018
 */
public class SleepingApplication {

    /**
     * A private constructor to ensure no construction.
     */
    private SleepingApplication() {
        // ensure no construction
    }

    /**
     * Application Entry Point.
     * 
     * @param args the command line arguments
     */
    public static void main(final String[] args)
            throws Exception {

        // obtain the Duration to sleep
        final Duration duration = args.length != 1 ? Duration.ofSeconds(2) : Duration.parse(args[0]);

        System.out.println("Commenced sleeping for " + duration);

        Thread.sleep(duration.toMillis());

        System.out.println("Terminated (successfully finished sleeping)");
    }
}
