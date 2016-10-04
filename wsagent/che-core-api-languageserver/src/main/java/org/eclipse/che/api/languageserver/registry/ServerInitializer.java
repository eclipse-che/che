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
package org.eclipse.che.api.languageserver.registry;

import io.typefox.lsapi.services.LanguageServer;

import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;

import java.util.Map;

/**
 * Is responsible to start new {@link LanguageServer}.
 *
 * @author Anatoliy Bazko
 */
public interface ServerInitializer extends ServerInitializerObservable {
    /**
     * Initialize new {@link LanguageServer} with given project path.
     */
    LanguageServer initialize(LanguageServerLauncher launcher, String projectPath) throws LanguageServerException;

    /**
     * Returns initialized servers.
     */
    Map<LanguageServer, LanguageServerDescription> getInitializedServers();
}
