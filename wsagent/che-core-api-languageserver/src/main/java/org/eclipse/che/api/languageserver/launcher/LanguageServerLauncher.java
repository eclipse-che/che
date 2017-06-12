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

import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.registry.LanguageServerDescription;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * @author Anatoliy Bazko
 */
public interface LanguageServerLauncher {

    /**
     * Starts {@link io.typefox.lsapi.services.LanguageServer}.
     */
    LanguageServer launch(String projectPath, LanguageClient client) throws LanguageServerException;

    /**
     * Gets supported language ID.
     */
    LanguageServerDescription getDescription();

    boolean isAbleToLaunch();
}
