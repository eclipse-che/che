/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.languageserver.launcher;

import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Anatolii Bazko
 */
public abstract class LanguageServerLauncherTemplate implements LanguageServerLauncher {

    private static Logger LOGGER = LoggerFactory.getLogger(LanguageServerLauncherTemplate.class);

    @Override
    public final LanguageServer launch(String projectPath, LanguageClient client) throws LanguageServerException {
        Process languageServerProcess = startLanguageServerProcess(projectPath);
        waitCheckProcess(languageServerProcess);
        return connectToLanguageServer(languageServerProcess, client);
    }

    /**
     * Temporary solution, in future need to provide some service that can watch for LS process and notify user in case in some reason it
     * stopped.
     * For now we just check it once start it before connect to it.
     * Ask with delay in 5 seconds this delay chose empirical in normal state should be enough for start or fail process.
     * If after 5 seconds process not alive we notify client about problem.
     * @param languageServerProcess
     * @throws LanguageServerException
     */
    private void waitCheckProcess(Process languageServerProcess)  throws LanguageServerException {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            //ignore
        }
        if(!languageServerProcess.isAlive()) {
            final String error;
            try {
                error = IoUtil.readStream(languageServerProcess.getErrorStream());
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
                throw new LanguageServerException("Can't start language server process");
            }
            LOGGER.error("Can't start language server process. Got error: {}", error);
            throw new LanguageServerException(error);
        }

    }

    abstract protected Process startLanguageServerProcess(String projectPath) throws LanguageServerException;

    abstract protected LanguageServer connectToLanguageServer(Process languageServerProcess, LanguageClient client)
            throws LanguageServerException;



}
