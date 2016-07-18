/*
 * *****************************************************************************
 *  Copyright (c) 2012-2016 Codenvy, S.A.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Codenvy, S.A. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.plugin.languageserver.server.factory;

import io.typefox.lsapi.LanguageDescription;
import io.typefox.lsapi.services.LanguageServer;
import io.typefox.lsapi.services.json.JsonBasedLanguageServer;

import org.eclipse.che.plugin.languageserver.server.exception.LanguageServerException;
import org.eclipse.che.plugin.languageserver.shared.model.ServerConfiguration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Anatoliy Bazko
 */
public class ConfigurationBasedLanguageServerFactory extends LanguageServerFactoryTemplate {
    private final ServerConfiguration serverConfiguration;

    public ConfigurationBasedLanguageServerFactory(ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
    }

    @Override
    protected LanguageServer connectToLanguageServer(Process languageServerProcess) throws LanguageServerException {
        JsonBasedLanguageServer languageServer = new JsonBasedLanguageServer();
        languageServer.connect(languageServerProcess.getInputStream(), languageServerProcess.getOutputStream());
        return languageServer;
    }

    @Override
    protected Process startLanguageServerProcess(String projectPath) throws LanguageServerException {
        File tmpFile = writeInstallScriptToFile();

        ProcessBuilder processBuilder = new ProcessBuilder("bash", tmpFile.getAbsolutePath(), projectPath);
        processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        try {
            return processBuilder.start();
        } catch (IOException e) {
            throw new LanguageServerException("Can't start " + getLanguageDescription().getLanguageId() + " language server", e);
        }
    }

    private File writeInstallScriptToFile() throws LanguageServerException {
        File tmpFile;
        try {
            tmpFile = File.createTempFile("lserver", serverConfiguration.getLanguageDescription().getLanguageId());
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile))) {
                for (String cmd : serverConfiguration.getInstallScript()) {
                    writer.write(cmd);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new LanguageServerException("Can't prepare install script", e);
        }
        return tmpFile;
    }

    @Override
    public LanguageDescription getLanguageDescription() {
        return serverConfiguration.getLanguageDescription();
    }
}
