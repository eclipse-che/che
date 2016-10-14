/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.composer.server;

import com.google.inject.Singleton;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.core.model.project.SourceStorage;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.LineConsumerFactory;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.importer.ProjectImporter;
import org.eclipse.che.plugin.composer.shared.Constants;

import javax.validation.constraints.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kaloyan Raev
 */
@Singleton
public class ComposerProjectImporter implements ProjectImporter {

    @Override
    public String getId() {
        return Constants.COMPOSER_IMPORTER_ID;
    }

    @Override
    public boolean isInternal() {
        return false;
    }

    @Override
    public String getDescription() {
        return "Import project from a Composer package.";
    }

    @Override
    public ImporterCategory getCategory() {
        return ImporterCategory.PACKAGE;
    }

    @Override
    public void importSources(FolderEntry baseFolder, SourceStorage storage)
            throws ForbiddenException, ConflictException, UnauthorizedException, IOException, ServerException {
        importSources(baseFolder, storage, LineConsumerFactory.NULL);
    }

    @Override
    public void importSources(FolderEntry baseFolder, SourceStorage storage,
            LineConsumerFactory importOutputConsumerFactory)
            throws ForbiddenException, ConflictException, UnauthorizedException, IOException, ServerException {
        String packageName = storage.getLocation();
        String projectPath = baseFolder.getVirtualFile().toIoFile().getAbsolutePath();
        Process process = execute(packageName, projectPath);
        List<String> output = readOutput(process, importOutputConsumerFactory);
        checkForErrors(process, output);
    }

    private Process execute(@NotNull String packageName, @NotNull String projectPath) throws IOException {
        return new ProcessBuilder("composer", "create-project", packageName, projectPath, "--no-progress")
                .redirectErrorStream(true).start();
    }

    private List<String> readOutput(@NotNull Process process, @NotNull LineConsumerFactory outputConsumerFactory)
            throws IOException {
        List<String> output = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), Charset.defaultCharset()))) {
            LineConsumer lineConsumer = outputConsumerFactory.newLineConsumer();
            String line = null;
            while ((line = reader.readLine()) != null) {
                // aggregate output for later analysis
                output.add(line);
                // filter empty, "Downloading" and "Load from cache" lines
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !"Downloading".equals(trimmed) && !"Loading from cache".equals(trimmed)) {
                    // print progress to the line consumer
                    lineConsumer.writeLine(line);
                }
            }
        }
        return output;
    }

    private void checkForErrors(@NotNull Process process, @NotNull List<String> output)
            throws IOException, ServerException {
        try {
            int exitCode = process.waitFor();
            ComposerOutputAnalyzer.checkForErrors(exitCode, output);
        } catch (InterruptedException e) {
            throw new ServerException(e);
        }
    }
}
