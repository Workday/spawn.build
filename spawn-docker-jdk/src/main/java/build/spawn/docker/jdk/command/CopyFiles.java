package build.spawn.docker.jdk.command;

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

import build.base.archiving.TarBuilder;
import build.spawn.docker.Container;
import build.spawn.docker.jdk.HttpTransport;
import jakarta.inject.Inject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A command to copy a {@code TAR} archive in the local file system to a directory in a {@link Container} and extract
 * the contents in the destination directory.
 *
 * @author anand.sankaran
 * @since Aug-2022
 */
public class CopyFiles
    extends AbstractBlockingCommand<Container> {

    /**
     * The archive on the local file system.
     */
    private final Path archivePath;

    /**
     * The destination directory.
     */
    private final String destinationDirectory;

    /**
     * The current container.
     */
    @Inject
    private volatile Container container;

    /**
     * Constructor.
     *
     * @param archivePath          the location where to create the {@code TAR} file locally
     * @param destinationDirectory destination directory
     * @param filesToCopy          one or more files to copy
     */
    public CopyFiles(final Path archivePath, final String destinationDirectory, final Path... filesToCopy) {
        if (archivePath.toFile().length() > 0) {
            throw new IllegalArgumentException("TAR file is not empty");
        }

        if (filesToCopy.length < 1) {
            throw new IllegalArgumentException("At least one file should be passed in");
        }

        try {
            final TarBuilder tarBuilder = new TarBuilder();
            for (final Path filePath : filesToCopy) {
                tarBuilder.content().add(filePath);
            }

            tarBuilder.build(archivePath);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        this.archivePath = archivePath;
        this.destinationDirectory = destinationDirectory;
    }

    @Override
    protected HttpTransport.Request createRequest() {
        try {
            return HttpTransport.Request
                .put("/containers/" + this.container.id() + "/archive?path=" + this.destinationDirectory,
                    Files.readAllBytes(this.archivePath))
                .withContentType("application/octet-stream");
        } catch (final IOException e) {
            throw new RuntimeException("Failed to read archive", e);
        }
    }

    @Override
    protected Container createResult(final HttpTransport.Response response)
        throws IOException {
        // 400 Bad Parameter
        // 403 permission denied (read only)
        // 404 no such destination path found
        // 500 server error
        if (response.statusCode() != 200) {
            throw new RuntimeException("CopyFiles failed with HTTP " + response.statusCode());
        }
        return this.container;
    }
}
