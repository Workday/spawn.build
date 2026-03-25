package build.spawn.application.facet.facets;

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

public interface Address {

    int getNumber();

    String getStreet();

    String getCity();

    String getState();

    int getZipCode();

    class Implementation
        implements Address {

        @Override
        public int getNumber() {
            return 6140;
        }

        @Override
        public String getStreet() {
            return "Stoneridge Mall Road";
        }

        @Override
        public String getCity() {
            return "Pleasanton";
        }

        @Override
        public String getState() {
            return "CA";
        }

        @Override
        public int getZipCode() {
            return 94588;
        }
    }
}
