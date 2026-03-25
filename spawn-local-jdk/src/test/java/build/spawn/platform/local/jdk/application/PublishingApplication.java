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

import build.spawn.jdk.Publishing;

import java.io.Serializable;

/**
 * An application that publishes Serializable items.
 *
 * @author lina.xu
 * @since Apr-2021
 */
public class PublishingApplication {

    private PublishingApplication() {
    }

    /**
     * A {@link Serializable} item that can be published and subscribed.
     */
    public static class Item
        implements Serializable {

        /**
         * The name of the {@link Item}.
         */
        private final String name;

        /**
         * The message related to the {@link Item}.
         */
        private final String message;

        /**
         * Constructs a {@link Item}.
         *
         * @param name    the name
         * @param message the message
         */
        private Item(final String name, final String message) {
            this.name = name;
            this.message = message;
        }

        /**
         * Obtains the name of the {@link Item}.
         *
         * @return the name
         */
        public String name() {
            return this.name;
        }

        /**
         * Obtains the message of the {@link Item}.
         *
         * @return the message
         */
        public String message() {
            return this.message;
        }
    }

    public static void main(final String[] args) {

        final var producerName = args[0];

        // get the producer to produce TestItems
        Publishing.createProducer(producerName, Item.class)
            .ifPresent(producer -> {
                producer.publish(new Item("GOT", "episode1"));
                producer.publish(new Item("GOT", "episode2"));
                producer.publish(new Item("GOT", "episode3"));
            });
    }
}
