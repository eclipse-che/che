/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.csharp.languageserver;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncherTemplate;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.plugin.csharp.inject.CSharpModule;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * @author Evgen Vidolob
 */
@Singleton
public class CSharpLanguageServerLauncher extends LanguageServerLauncherTemplate {

 
    private final Path launchScript;

    @Inject
    public CSharpLanguageServerLauncher() {
        launchScript = Paths.get(System.getenv("HOME"), "che/ls-csharp/launch.sh");
    }

    @Override
    protected Process startLanguageServerProcess(String projectPath) throws LanguageServerException {
        restoreDependencies(projectPath);

        ProcessBuilder processBuilder = new ProcessBuilder(launchScript.toString());
        processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);

        try {
            return processBuilder.start();
        } catch (IOException e) {
            throw new LanguageServerException("Can't start CSharp language server", e);
        }
    }

    private void restoreDependencies(String projectPath) throws LanguageServerException {
        ProcessBuilder processBuilder = new ProcessBuilder("dotnet", "restore");
        processBuilder.directory(new File(projectPath));
        try {
            Process process = processBuilder.start();
            int resultCode = process.waitFor();
            if (resultCode != 0) {
                String err = IoUtil.readStream(process.getErrorStream());
                String in = IoUtil.readStream(process.getInputStream());
                throw new LanguageServerException("Can't restore dependencies. Error: " + err + ". Output: " + in);
            }
        } catch (IOException | InterruptedException e) {
            throw new LanguageServerException("Can't start CSharp language server", e);
        }
    }

    @Override
    protected LanguageServer connectToLanguageServer(final Process languageServerProcess, LanguageClient client) {
        Launcher<LanguageServer> launcher = Launcher.createLauncher(client, LanguageServer.class, languageServerProcess.getInputStream(),
                                                                    languageServerProcess.getOutputStream());
        launcher.startListening();
        return launcher.getRemoteProxy();
    }

    @Override
    public String getLanguageId() {
        return CSharpModule.LANGUAGE_ID;
    }

    @Override
    public boolean isAbleToLaunch() {
        return Files.exists(launchScript);
    }

 }
