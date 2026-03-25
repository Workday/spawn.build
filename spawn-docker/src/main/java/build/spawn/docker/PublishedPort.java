package build.spawn.docker;

/*-
 * #%L
 * Spawn Docker (Client)
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

import build.spawn.docker.option.ExposedPort;

import java.net.InetSocketAddress;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An {@link ExposedPort} published for a {@link Container}.
 *
 * @author brian.oliver
 * @since Aug-2021
 */
public final class PublishedPort {

    /**
     * The {@link ExposedPort} published for the {@link Container}.
     */
    private final ExposedPort exposedPort;

    /**
     * The {@link InetSocketAddress} that may be used to communicate with the {@link Container} {@link ExposedPort}.
     */
    private final LinkedHashSet<InetSocketAddress> addresses;

    /**
     * Constructs a {@link PublishedPort}.
     *
     * @param exposedPort the {@link ExposedPort} that has been published
     * @param addresses   the {@link InetSocketAddress}es on which the {@link ExposedPort} has been published.
     */
    private PublishedPort(final ExposedPort exposedPort,
                          final Stream<InetSocketAddress> addresses) {

        this.exposedPort = Objects.requireNonNull(exposedPort, "The ExposedPort must not be null");
        this.addresses = Objects.requireNonNull(addresses, "The Addresses must not be null")
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Obtains the {@link ExposedPort} that has been published.
     *
     * @return the published {@link ExposedPort}
     */
    public ExposedPort getExposedPort() {
        return this.exposedPort;
    }

    /**
     * Obtains the {@link Stream} of {@link InetSocketAddress}es that may be used to communicate with the
     * {@link Container} {@link ExposedPort}.
     *
     * @return a {@link Stream} of {@link InetSocketAddress}es
     */
    public Stream<InetSocketAddress> addresses() {
        return this.addresses.stream();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PublishedPort that = (PublishedPort) o;
        return this.exposedPort.equals(that.exposedPort) && this.addresses.equals(that.addresses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.exposedPort, this.addresses);
    }

    @Override
    public String toString() {
        return "PublishedPort{"
            + this.exposedPort
            + ", Addresses=[" + this.addresses.stream()
            .map(InetSocketAddress::toString)
            .collect(Collectors.joining(", "))
            + "]}";
    }

    /**
     * Creates a {@link PublishedPort} for a given {@link ExposedPort} and {@link InetSocketAddress}es.
     *
     * @param exposedPort the {@link ExposedPort}
     * @param addresses   the {@link Stream} of {@link InetSocketAddress}es
     * @return a new {@link PublishedPort}
     */
    public static PublishedPort of(final ExposedPort exposedPort,
                                   final Stream<InetSocketAddress> addresses) {
        return new PublishedPort(exposedPort, addresses);
    }
}
