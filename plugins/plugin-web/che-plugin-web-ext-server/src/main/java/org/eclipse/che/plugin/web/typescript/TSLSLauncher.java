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
package org.eclipse.che.plugin.web.typescript;

import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncherTemplate;
import org.eclipse.che.plugin.web.shared.Constants;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

import javax.inject.Singleton;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * Launcher for TypeScript Language Server
 */
@Singleton
public class TSLSLauncher extends LanguageServerLauncherTemplate {
    private final Path launchScript;

    public TSLSLauncher() {
        launchScript = Paths.get(System.getenv("HOME"), "che/ls-typescript/launch.sh");
    }

    @Override
    protected Process startLanguageServerProcess(String projectPath) throws LanguageServerException {
        ProcessBuilder processBuilder = new ProcessBuilder(launchScript.toString());
        processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        try {
            return processBuilder.start();
        } catch (IOException e) {
            throw new LanguageServerException("Can't start TypeScript language server", e);
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
        return Constants.TS_LANG;
    }

    @Override
    public boolean isAbleToLaunch() {
        return Files.exists(launchScript);
    }
}
