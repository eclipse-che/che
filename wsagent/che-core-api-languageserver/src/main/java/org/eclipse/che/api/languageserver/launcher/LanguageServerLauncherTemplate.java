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
package org.eclipse.che.api.languageserver.launcher;

import io.typefox.lsapi.services.LanguageServer;

import org.eclipse.che.api.languageserver.exception.LanguageServerException;

/**
 * @author Anatolii Bazko
 */
public abstract class LanguageServerLauncherTemplate implements LanguageServerLauncher {

    @Override
    public final LanguageServer launch(String projectPath) throws LanguageServerException {
        Process languageServerProcess = startLanguageServerProcess(projectPath);
        return connectToLanguageServer(languageServerProcess);
    }

    abstract protected Process startLanguageServerProcess(String projectPath) throws LanguageServerException;

    abstract protected LanguageServer connectToLanguageServer(Process languageServerProcess) throws LanguageServerException;
}
