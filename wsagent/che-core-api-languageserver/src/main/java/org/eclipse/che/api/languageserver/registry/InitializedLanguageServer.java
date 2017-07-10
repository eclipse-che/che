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
package org.eclipse.che.api.languageserver.registry;

import org.eclipse.che.api.languageserver.launcher.LanguageServerLauncher;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * Simple container for {@link LanguageServerLauncher}, {@link InitializeResult}
 * and {@link LanguageServer}
 *
 * @author Evgen Vidolob
 */
public class InitializedLanguageServer {
    private final String                 id;
    private final LanguageServer         server;
    private final InitializeResult       initializeResult;
    private final LanguageServerLauncher launcher;

    public InitializedLanguageServer(String id, LanguageServer server, InitializeResult initializeResult, LanguageServerLauncher launcher) {
        this.id = id;
        this.server = server;
        this.initializeResult = initializeResult;
        this.launcher = launcher;
    }
    
    public String getId() {
        return id;
    }

    public LanguageServer getServer() {
        return server;
    }

    public InitializeResult getInitializeResult() {
        return initializeResult;
    }

    public LanguageServerLauncher getLauncher() {
        return launcher;
    }
}
