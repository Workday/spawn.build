package build.spawn.docker.jdk.model;

/*-
 * #%L
 * Spawn Docker (JDK Client)
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

import build.spawn.docker.Execution;

import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * An implementation of {@link Execution.Information}.
 *
 * @author brian.oliver
 * @since Sep-2021
 */
public class ExecutionInformation
    extends AbstractJsonBasedResult
    implements Execution.Information {

    @Override
    public String getContainerId() {
        return jsonNode().get("ContainerId").asText();
    }

    @Override
    public String id() {
        return jsonNode().get("ID").asText();
    }

    @Override
    public OptionalLong pid() {
        final var pid = jsonNode().get("Pid");

        return pid == null || pid.isMissingNode() || pid.longValue() == 0
            ? OptionalLong.empty()
            : OptionalLong.of(pid.longValue());
    }

    @Override
    public OptionalInt exitValue() {
        final var exitValue = jsonNode().get("ExitCode");

        return exitValue == null || exitValue.isMissingNode()
            ? OptionalInt.empty()
            : OptionalInt.of(exitValue.intValue());
    }
}
