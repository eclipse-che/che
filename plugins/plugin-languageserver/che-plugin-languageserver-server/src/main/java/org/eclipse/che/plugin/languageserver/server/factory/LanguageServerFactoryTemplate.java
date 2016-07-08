/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.languageserver.server.factory;

import io.typefox.lsapi.services.LanguageServer;

import com.google.inject.Singleton;

import org.eclipse.che.plugin.languageserver.server.exception.LanguageServerException;

/**
 * @author Anatolii Bazko
 */
@Singleton
public abstract class LanguageServerFactoryTemplate implements LanguageServerFactory {

    @Override
    public LanguageServer create(String projectPath) throws LanguageServerException {
        Process languageServerProcess = startLanguageServerProcess(projectPath);
        return connectToLanguageServer(languageServerProcess);
    }

    abstract protected Process startLanguageServerProcess(String projectPath) throws LanguageServerException;

    abstract protected LanguageServer connectToLanguageServer(Process languageServerProcess) throws LanguageServerException;
}
