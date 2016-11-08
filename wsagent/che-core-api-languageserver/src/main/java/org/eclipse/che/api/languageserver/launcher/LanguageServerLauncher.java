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
package org.eclipse.che.api.languageserver.launcher;

import io.typefox.lsapi.services.LanguageServer;

import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;

/**
 * @author Anatoliy Bazko
 */
public interface LanguageServerLauncher {

    /**
     * Starts {@link io.typefox.lsapi.services.LanguageServer}.
     */
    LanguageServer launch(String projectPath) throws LanguageServerException;

    /**
     * Gets supported languages.
     */
    LanguageDescription getLanguageDescription();

    boolean isAbleToLaunch();
}
