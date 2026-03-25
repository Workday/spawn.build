package build.spawn.application.option;

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

import build.base.configuration.CollectedOption;
import build.base.configuration.Option;
import build.base.flow.Subscriber;
import build.spawn.application.Application;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * An {@link Option} to define a {@link Subscriber} for {@link Serializable} items published from the launched
 * {@link Application}.
 *
 * @param <T> the type of the {@link Object}
 * @author lina.xu
 * @since Apr-2021
 */
public class ApplicationSubscriber<T extends Serializable>
    implements CollectedOption<List> {

    /**
     * The name of the {@link Subscriber}.
     */
    private final String subscriberName;

    /**
     * The {@link Class} of items to which to subscribe.
     */
    private final Class<? extends T> itemClass;

    /**
     * The {@link Subscriber}.
     */
    private final Subscriber<? super T> subscriber;

    /**
     * Constructs an {@link ApplicationSubscriber}.
     *
     * @param subscriberName the name of the {@link Subscriber}
     * @param itemClass      the {@link Class} of the item to observe
     * @param subscriber     the {@link Subscriber}
     */
    protected ApplicationSubscriber(final String subscriberName,
                                    final Class<? extends T> itemClass,
                                    final Subscriber<? super T> subscriber) {

        this.subscriberName = Objects.requireNonNull(subscriberName, "The Subscriber name must not be null");
        this.itemClass = Objects.requireNonNull(itemClass, "The class of items must not be null");
        this.subscriber = Objects.requireNonNull(subscriber, "The Subscriber must not be null");
    }

    /**
     * Obtains the publisher name to observe.
     *
     * @return the publisher name
     */
    public String name() {
        return this.subscriberName;
    }

    /**
     * Obtains the {@link Class} of the items to observe.
     *
     * @return the {@link Class} of the item
     */
    public Class<? extends T> itemClass() {
        return this.itemClass;
    }

    /**
     * Obtains the {@link Subscriber}.
     *
     * @return the {@link Subscriber}
     */
    public Subscriber<? super T> subscriber() {
        return this.subscriber;
    }

    /**
     * Creates an {@link ApplicationSubscriber} from the specified {@link Subscriber} name, subscribed item class and
     * the {@link Subscriber}.
     *
     * @param subscriberName the name of the {@link Subscriber}
     * @param itemClass      the {@link Class} of the item to subscribe
     * @param subscriber     the {@link Subscriber}
     * @param <T>            the type of item {@link Class}
     * @return an {@link ApplicationSubscriber}
     */
    public static <T extends Serializable> ApplicationSubscriber<T> of(final String subscriberName,
                                                                       final Class<? extends T> itemClass,
                                                                       final Subscriber<? super T> subscriber) {

        return new ApplicationSubscriber<>(subscriberName, itemClass, subscriber);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        final ApplicationSubscriber<T> that = (ApplicationSubscriber<T>) other;
        return Objects.equals(this.subscriber, that.subscriber) && Objects.equals(this.itemClass, that.itemClass)
            && Objects.equals(this.subscriberName, that.subscriberName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.subscriberName, this.itemClass, this.subscriber);
    }

    @Override
    public String toString() {
        return "ApplicationSubscriber{"
            + this.subscriberName + " "
            + this.itemClass.getCanonicalName() + " "
            + this.subscriber + '}';
    }
}
