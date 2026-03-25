package build.spawn.platform.local.jdk.application;

/*-
 * #%L
 * Spawn Local JDK
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

import build.base.commandline.CommandLine;
import build.base.commandline.CommandLineParser;
import build.base.configuration.AbstractValueOption;
import build.base.configuration.Option;

import java.util.stream.Stream;

/**
 * A simple Java application that parses command line options for testing purposes.
 * <p>
 * Allowable options:
 * -t --text: If present, print the text to stdout, otherwise, "Hello World"
 * -e --exitValue: If present, return with this value, otherwise -1
 *
 * @author spencer.firestone
 * @since Sep-2020
 */
public class CommandLineParserApplication {

    /**
     * A private constructor to ensure no construction.
     */
    private CommandLineParserApplication() {
        // ensure no construction
    }

    /**
     * Application Entry Point.
     *
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        final var configuration = new CommandLineParser()
            .add(ExitValueOption.class)
            .add(TextOption.class)
            .parse(args)
            .build();

        final var text = configuration.getOptionalValue(TextOption.class)
            .orElse("Hello World");

        final var exitValue = configuration.getOptionalValue(ExitValueOption.class)
            .orElse(0);

        System.out.println(text);

        System.exit(exitValue);
    }

    /**
     * A {@link CommandLine} {@link Option} that produces an int value.
     */
    public static class ExitValueOption
        extends AbstractValueOption<Integer>
        implements CommandLine {

        private ExitValueOption(final int value) {
            super(value);
        }

        @CommandLine.Prefix("-e")
        @CommandLine.Prefix("--exit-value")
        public static ExitValueOption of(final int value) {
            return new ExitValueOption(value);
        }

        @Override
        public Stream<String> arguments() {
            return Stream.of("-e", String.valueOf(get()));
        }
    }

    /**
     * A {@link CommandLine} {@link Option} that produces a String value.
     */
    public static class TextOption
        extends AbstractValueOption<String>
        implements CommandLine {

        private TextOption(final String value) {
            super(value);
        }

        @CommandLine.Prefix("-t")
        @CommandLine.Prefix("--text")
        public static TextOption of(final String value) {
            return new TextOption(value);
        }

        @Override
        public Stream<String> arguments() {
            return Stream.of("-t", get());
        }
    }
}
