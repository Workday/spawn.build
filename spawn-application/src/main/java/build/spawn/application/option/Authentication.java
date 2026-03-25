package build.spawn.application.option;

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

import build.base.configuration.CollectedOption;
import build.base.configuration.Option;
import build.base.table.Table;
import build.base.table.Tabular;
import build.spawn.application.Application;

import java.util.List;
import java.util.Objects;

/**
 * An {@link Option} to pass authentication information to {@link Application}s.
 *
 * @author graeme.campbell
 * @since Mar-2020
 */
@SuppressWarnings("rawtypes")
public interface Authentication
    extends CollectedOption<List>, Tabular {

    /**
     * An implementation of {@link Authentication} for the username/password auth strategy
     */
    class UsernamePassword
        implements Authentication {

        private static final String OBFUSCATED_PASSWORD = "********";

        private final String username;

        private final String password;

        private UsernamePassword(final String username, final String password) {

            Objects.requireNonNull(username, "Username cannot be null");
            Objects.requireNonNull(password, "Password cannot be null");

            this.username = username;
            this.password = password;
        }

        /**
         * Gets the username from this {@link UsernamePassword} pair.
         *
         * @return this {@link UsernamePassword}'s username
         */
        public String getUsername() {
            return this.username;
        }

        /**
         * Gets the password from this {@link UsernamePassword} pair.
         *
         * @return this {@link UsernamePassword}'s password
         */
        public String getPassword() {
            return this.password;
        }

        @Override
        public void tabularize(final Table table) {
            table.addRow("Authentication", "Username:", this.username, "Password:", OBFUSCATED_PASSWORD);
        }

        @Override
        public boolean equals(final Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }
            final UsernamePassword that = (UsernamePassword) other;
            return Objects.equals(this.username, that.username) && Objects.equals(this.password, that.password);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.username, this.password);
        }

        @Override
        public String toString() {
            return getClass().getName() + "{Username=" + this.username + "|Password=" + OBFUSCATED_PASSWORD + '}';
        }
    }

    /**
     * Create a new {@link Authentication} {@link Option} for username/password authentication strategies.
     *
     * @param username the username for which authentication should be established
     * @param password the password with which the user may authenticate
     * @return a new {@link Authentication} {@link Option}
     */
    static Authentication usernamePassword(final String username, final String password) {
        return new UsernamePassword(username, password);
    }
}
