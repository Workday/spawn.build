package build.spawn.docker.okhttp;

/*-
 * #%L
 * Spawn Docker (OkHttp Client)
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

import build.base.configuration.Configuration;
import build.codemodel.injection.InjectionFramework;
import build.spawn.docker.okhttp.option.ConnectTimeout;
import build.spawn.docker.okhttp.option.ReadTimeout;
import build.spawn.docker.okhttp.option.WriteTimeout;
import okhttp3.ConnectionPool;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * A TCP-{@link Socket} based {@code Docker} {@link build.spawn.docker.Session}.
 *
 * @author brian.oliver
 * @since Aug-2022
 */
public class TCPSocketBasedSession
    extends AbstractSession {

    /**
     * Constructs a {@link TCPSocketBasedSession} for the specified API version using the {@link InetSocketAddress}.
     *
     * @param injectionFramework the {@link InjectionFramework}
     * @param socketAddress      the {@link InetSocketAddress}
     * @param configuration      the {@link Configuration}
     */
    public TCPSocketBasedSession(final InjectionFramework injectionFramework,
                                 final InetSocketAddress socketAddress,
                                 final Configuration configuration) {

        super(injectionFramework,
            () -> {
                final Duration connectTimeout = configuration
                    .getOptionalValue(ConnectTimeout.class)
                    .orElse(Duration.of(10, ChronoUnit.SECONDS));

                final Duration readTimeout = configuration
                    .getOptionalValue(ReadTimeout.class)
                    .orElse(Duration.of(30, ChronoUnit.SECONDS));

                final Duration writeTimeout = configuration
                    .getOptionalValue(WriteTimeout.class)
                    .orElse(Duration.of(10, ChronoUnit.SECONDS));

                final OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .connectTimeout(connectTimeout.getSeconds(), TimeUnit.SECONDS)
                    .writeTimeout(writeTimeout.getSeconds(), TimeUnit.SECONDS)
                    .readTimeout(readTimeout.getSeconds(), TimeUnit.SECONDS)
                    .connectionPool(new ConnectionPool(0, 1, TimeUnit.SECONDS));

                return builder.build();
            },
            () -> new HttpUrl.Builder()
                .scheme("http")
                .host(socketAddress.getHostString())
                .port(socketAddress.getPort()),
            configuration);
    }
}
