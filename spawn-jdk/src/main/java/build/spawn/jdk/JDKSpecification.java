package build.spawn.jdk;

/*-
 * #%L
 * Spawn JDK
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

import build.spawn.application.Specification;

/**
 * A concrete {@link Specification} for a {@link JDKApplication}.
 *
 * @author brian.oliver
 * @since Aug-2021
 */
public final class JDKSpecification
    extends AbstractJDKSpecification<JDKApplication, JDKSpecification> {

    @Override
    public Class<? extends JDKApplication> getApplicationClass() {
        return JDKApplication.class;
    }
}
