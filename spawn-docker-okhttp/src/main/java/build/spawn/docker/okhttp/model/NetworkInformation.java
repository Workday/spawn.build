package build.spawn.docker.okhttp.model;

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

import build.spawn.docker.Network;

/**
 * Class representing a {@link Network}s information.
 *
 * @author anand.sankaran
 * @since Aug-2022
 */
public class NetworkInformation
    extends AbstractJsonBasedResult
    implements Network.Information {

    @Override
    public String driver() {
        return jsonNode().get("driver").asText();
    }

    @Override
    public String id() {
        return jsonNode().get("Id").asText();
    }

    @Override
    public String name() {
        return jsonNode().get("Name").asText();
    }

    @Override
    public boolean enableIPv6() {
        return jsonNode().get("EnableIPv6").asBoolean();
    }
}
