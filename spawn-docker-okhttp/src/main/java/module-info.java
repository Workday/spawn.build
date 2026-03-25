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
/**
 * Module descriptor for build.spawn.docker.
 *
 * @author brian.oliver
 * @since Nov-2024
 */
open module build.spawn.docker.okhttp {
    requires transitive build.spawn.option;
    requires transitive build.spawn.docker;

    requires build.codemodel.foundation;
    requires build.codemodel.jdk;
    requires build.codemodel.injection;

    requires transitive build.base.naming;

    requires okhttp3;
    requires kotlin.stdlib; // this is to prevent okhttp3 deprecation errors in the compiler
    requires jakarta.inject;
    requires org.newsclub.net.unix;
    requires build.base.io;
    requires com.fasterxml.jackson.databind;
    requires build.base.archiving;
    requires build.base.option;
    requires build.base.flow;
    requires java.logging;

    exports build.spawn.docker.okhttp;

    provides build.spawn.docker.Session.Factory with
        build.spawn.docker.okhttp.UnixDomainSocketBasedSession.Factory,
        build.spawn.docker.okhttp.LocalHostBasedSessionFactory,
        build.spawn.docker.okhttp.DockerHostVariableBasedSessionFactory,
        build.spawn.docker.okhttp.InternalDockerHostBasedSessionFactory;
}
