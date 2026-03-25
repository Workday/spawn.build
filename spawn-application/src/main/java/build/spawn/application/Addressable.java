package build.spawn.application;

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

import java.net.InetAddress;
import java.util.stream.Stream;

/**
 * An entity that has one or more {@link InetAddress}es.
 *
 * @author brian.oliver
 * @since Jun-2020
 */
public interface Addressable {

    /**
     * Obtains the {@link InetAddress}es, in order of their defined preference.
     *
     * @return {@link Stream} of {@link InetAddress}
     */
    Stream<InetAddress> addresses();
}
