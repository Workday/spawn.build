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

import build.spawn.application.option.LaunchIdentity;

import java.time.Duration;

/**
 * A Java application that prints messages for a specified amount of time before hanging forever.
 *
 * @author ben.horowitz
 * @since Jan-2020
 */
public class PrintingApplication {

    private PrintingApplication() {
    }

    /**
     * Application Entry Point.
     *
     * @param args the command line arguments
     */
    public static void main(final String[] args)
        throws InterruptedException {

        final Duration duration = args.length != 1 ? Duration.ofSeconds(5) : Duration.parse(args[0]);

        final long start = System.currentTimeMillis();

        final String app = System.getProperty("name", "(undefined)");

        for (int i = 0; Math.abs(System.currentTimeMillis() - start) < duration.toMillis(); i++) {
            System.out.println(String.format("[%s] iteration %d", app, i));
            Thread.sleep(200);
        }

        Thread.sleep(Long.MAX_VALUE);
    }
}
