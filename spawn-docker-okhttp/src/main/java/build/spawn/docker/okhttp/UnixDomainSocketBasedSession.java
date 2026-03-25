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
import build.spawn.docker.Session;
import build.codemodel.injection.InjectionFramework;
import jakarta.inject.Inject;
import okhttp3.ConnectionPool;
import okhttp3.Dns;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

import javax.net.SocketFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * A Unix Domain Socket-based {@code Docker} {@link Session}.
 *
 * @author brian.oliver
 * @since Jun-2021
 */
public class UnixDomainSocketBasedSession
    extends AbstractSession {

    /**
     * The {@code docker.sock} {@link File}.
     */
    private static final File DOCKER_SOCK_FILE = new File("/var/run/docker.sock");

    /**
     * Constructs a {@link UnixDomainSocketBasedSession} using the specified {@link Configuration}.
     *
     * @param injectionFramework the {@link InjectionFramework} for Dependency Injection
     * @param configuration      the {@link Configuration}
     */
    public UnixDomainSocketBasedSession(final InjectionFramework injectionFramework,
                                        final Configuration configuration) {

        this(injectionFramework, DOCKER_SOCK_FILE, configuration);
    }

    /**
     * Constructs a {@link UnixDomainSocketBasedSession} for the specified API version using the provided Unix Socket {@link File},
     * which is typically something like {@code /var/run/docker.sock}.
     *
     * @param injectionFramework the {@link InjectionFramework} for Dependency Injection
     * @param socketFile         the Unix Socket {@link File}
     * @param configuration      the {@link Configuration}
     */
    public UnixDomainSocketBasedSession(final InjectionFramework injectionFramework,
                                        final File socketFile,
                                        final Configuration configuration) {

        super(injectionFramework,
            () -> {
                final var builder = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .socketFactory(new UnixDomainSocketFactory(socketFile))
                    .connectionPool(new ConnectionPool(0, 1, TimeUnit.SECONDS))
                    .dns(hostname -> {
                        if (hostname.endsWith(".sock")) {
                            try {
                                return Collections.singletonList(AFUNIXSocketAddress.of(socketFile).getAddress());
                            } catch (final IOException e) {
                                throw new RuntimeException("Failed to open socket" + hostname, e);
                            }
                        } else {
                            return Dns.SYSTEM.lookup(hostname);
                        }
                    });

                return builder.build();
            },
            () -> new HttpUrl.Builder()
                .scheme("http")
                .host("docker.sock"),
            configuration);
    }

    /**
     * A {@link SocketFactory} for connecting to {@code unix://}-based {@link Socket} {@link File}s.
     */
    private static class UnixDomainSocketFactory
        extends SocketFactory {

        /**
         * The Unix {@link Socket}-based {@link File} to which to connect.
         */
        private final File socketFile;

        /**
         * Constructs a {@link UnixDomainSocketFactory} for the specified Unix {@link Socket}-based {@link File}.
         *
         * @param socketFile the Unix {@link Socket}-based {@link File}
         */
        private UnixDomainSocketFactory(final File socketFile) {
            this.socketFile = Objects.requireNonNull(socketFile, "The Socket File must not be null");
        }

        @Override
        public Socket createSocket()
            throws IOException {

            // create and connect the Unix-based Socket
            final var socket = AFUNIXSocket.newInstance();
            socket.connect(AFUNIXSocketAddress.of(this.socketFile));

            return new ConnectedDomainSocket(socket);
        }

        @Override
        public Socket createSocket(final String host, final int port) {

            throw new UnsupportedOperationException(
                "UnixDomainSocketFactory does not support InetAddress-based requests");
        }

        @Override
        public Socket createSocket(final String host,
                                   final int port,
                                   final InetAddress localHost,
                                   final int localPort) {

            throw new UnsupportedOperationException(
                "UnixDomainSocketFactory does not support InetAddress-based requests");
        }

        @Override
        public Socket createSocket(final InetAddress host, final int port) {

            throw new UnsupportedOperationException(
                "UnixDomainSocketFactory does not support InetAddress-based requests");
        }

        @Override
        public Socket createSocket(final InetAddress address,
                                   final int port,
                                   final InetAddress localAddress,
                                   final int localPort) {

            throw new UnsupportedOperationException(
                "UnixDomainSocketFactory does not support InetAddress-based requests");
        }

        /**
         * A {@link Socket} adapter that prevents re-connection and re-binding.
         */
        private static class ConnectedDomainSocket
            extends Socket {

            private final Socket socket;

            private ConnectedDomainSocket(final Socket socket) {
                this.socket = socket;
            }

            @Override
            public void connect(final SocketAddress endpoint) {
                // ignore reconnection
            }

            @Override
            public void connect(final SocketAddress endpoint, final int timeout) {
                // ignore reconnection
            }

            @Override
            public void bind(final SocketAddress bindpoint) {
                // ignore reconnection
            }

            @Override
            public InetAddress getInetAddress() {
                return this.socket.getInetAddress();
            }

            @Override
            public InetAddress getLocalAddress() {
                return this.socket.getLocalAddress();
            }

            @Override
            public int getPort() {
                return this.socket.getPort();
            }

            @Override
            public int getLocalPort() {
                return this.socket.getLocalPort();
            }

            @Override
            public SocketAddress getRemoteSocketAddress() {
                return this.socket.getRemoteSocketAddress();
            }

            @Override
            public SocketAddress getLocalSocketAddress() {
                return this.socket.getLocalSocketAddress();
            }

            @Override
            public SocketChannel getChannel() {
                return this.socket.getChannel();
            }

            @Override
            public InputStream getInputStream()
                throws IOException {
                return this.socket.getInputStream();
            }

            @Override
            public OutputStream getOutputStream()
                throws IOException {
                return this.socket.getOutputStream();
            }

            @Override
            public void setTcpNoDelay(final boolean on)
                throws SocketException {
                this.socket.setTcpNoDelay(on);
            }

            @Override
            public boolean getTcpNoDelay()
                throws SocketException {
                return this.socket.getTcpNoDelay();
            }

            @Override
            public void setSoLinger(final boolean on, final int linger)
                throws SocketException {
                this.socket.setSoLinger(on, linger);
            }

            @Override
            public int getSoLinger()
                throws SocketException {
                return this.socket.getSoLinger();
            }

            @Override
            public void sendUrgentData(final int data)
                throws IOException {
                this.socket.sendUrgentData(data);
            }

            @Override
            public void setOOBInline(final boolean on)
                throws SocketException {
                this.socket.setOOBInline(on);
            }

            @Override
            public boolean getOOBInline()
                throws SocketException {
                return this.socket.getOOBInline();
            }

            @Override
            public synchronized void setSoTimeout(final int timeout)
                throws SocketException {
                this.socket.setSoTimeout(timeout);
            }

            @Override
            public synchronized int getSoTimeout()
                throws SocketException {
                return this.socket.getSoTimeout();
            }

            @Override
            public synchronized void setSendBufferSize(final int size)
                throws SocketException {
                this.socket.setSendBufferSize(size);
            }

            @Override
            public synchronized int getSendBufferSize()
                throws SocketException {
                return this.socket.getSendBufferSize();
            }

            @Override
            public synchronized void setReceiveBufferSize(final int size)
                throws SocketException {
                this.socket.setReceiveBufferSize(size);
            }

            @Override
            public synchronized int getReceiveBufferSize()
                throws SocketException {
                return this.socket.getReceiveBufferSize();
            }

            @Override
            public void setKeepAlive(final boolean on)
                throws SocketException {
                this.socket.setKeepAlive(on);
            }

            @Override
            public boolean getKeepAlive()
                throws SocketException {
                return this.socket.getKeepAlive();
            }

            @Override
            public void setTrafficClass(final int tc)
                throws SocketException {
                this.socket.setTrafficClass(tc);
            }

            @Override
            public int getTrafficClass()
                throws SocketException {
                return this.socket.getTrafficClass();
            }

            @Override
            public void setReuseAddress(final boolean on)
                throws SocketException {
                this.socket.setReuseAddress(on);
            }

            @Override
            public boolean getReuseAddress()
                throws SocketException {
                return this.socket.getReuseAddress();
            }

            @Override
            public synchronized void close()
                throws IOException {
                this.socket.close();
            }

            @Override
            public void shutdownInput()
                throws IOException {
                this.socket.shutdownInput();
            }

            @Override
            public void shutdownOutput()
                throws IOException {
                this.socket.shutdownOutput();
            }

            @Override
            public String toString() {
                return this.socket.toString();
            }

            @Override
            public boolean isConnected() {
                return this.socket.isConnected();
            }

            @Override
            public boolean isBound() {
                return this.socket.isBound();
            }

            @Override
            public boolean isClosed() {
                return this.socket.isClosed();
            }

            @Override
            public boolean isInputShutdown() {
                return this.socket.isInputShutdown();
            }

            @Override
            public boolean isOutputShutdown() {
                return this.socket.isOutputShutdown();
            }

            @Override
            public void setPerformancePreferences(final int connectionTime, final int latency, final int bandwidth) {
                this.socket.setPerformancePreferences(connectionTime, latency, bandwidth);
            }
        }
    }

    /**
     * The {@link Session.Factory} for the {@link UnixDomainSocketBasedSession}s.
     */
    public static class Factory
        implements Session.Factory {

        /**
         * The {@link InjectionFramework} to use for Dependency Injection.
         */
        @Inject
        private InjectionFramework injectionFramework;

        @Override
        public boolean isOperational() {
            // attempt to connect to the docker.sock file
            try (var _ = new UnixDomainSocketFactory(DOCKER_SOCK_FILE).createSocket()) {
                return true;
            } catch (final Exception _) {
                return false;
            }
        }

        @Override
        public Optional<Session> create(final Configuration configuration) {
            return isOperational()
                ? Optional.of(new UnixDomainSocketBasedSession(this.injectionFramework, configuration))
                : Optional.empty();
        }
    }
}
