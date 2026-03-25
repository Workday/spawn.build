/*-
 * #%L
 * Spawn Application Composition
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
 * Module descriptor for build.spawn.application.composition
 *
 * @author brian.oliver
 * @since Dec-2024
 */
module build.spawn.application.composition {
    requires transitive build.spawn.option;
    requires transitive build.spawn.application;
    requires transitive build.spawn.jdk;

    exports build.spawn.application.composition;
}
