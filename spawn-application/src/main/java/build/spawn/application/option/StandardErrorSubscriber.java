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
import build.spawn.application.Console;

import java.util.List;
import java.util.Objects;

/**
 * An {@link Option} to define a {@link Subscriber} for {@link Console} stderr, allowing publishing and subscription to
 * individual lines of stderr output.
 *
 * @author brian.oliver
 * @since Oct-2018
 */
public class StandardErrorSubscriber
    implements CollectedOption<List> {

    /**
     * The {@link Subscriber}.
     */
    private final Subscriber<? super String> subscriber;

    /**
     * Constructs an {@link StandardErrorSubscriber}.
     *
     * @param subscriber the {@link Subscriber}
     */
    protected StandardErrorSubscriber(final Subscriber<? super String> subscriber) {
        this.subscriber = Objects.requireNonNull(subscriber, "The Subscriber must not be null");
    }

    /**
     * Obtains the {@link Subscriber}.
     *
     * @return the {@link Subscriber}
     */
    public Subscriber<? super String> get() {
        return this.subscriber;
    }

    /**
     * Creates an {@link StandardErrorSubscriber} from the specified {@link Subscriber}.
     *
     * @param subscriber the {@link Subscriber}
     * @return an {@link StandardErrorSubscriber}
     */
    public static StandardErrorSubscriber of(final Subscriber<? super String> subscriber) {
        return new StandardErrorSubscriber(subscriber);
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        final StandardErrorSubscriber that = (StandardErrorSubscriber) other;
        return Objects.equals(this.subscriber, that.subscriber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.subscriber);
    }

    @Override
    public String toString() {
        return "StandardErrorSubscriber{" + this.subscriber + '}';
    }
}
