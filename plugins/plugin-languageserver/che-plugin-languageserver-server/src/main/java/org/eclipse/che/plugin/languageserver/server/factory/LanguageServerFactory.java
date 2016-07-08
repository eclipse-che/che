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

import io.typefox.lsapi.LanguageDescription;
import io.typefox.lsapi.services.LanguageServer;

import org.eclipse.che.plugin.languageserver.server.exception.LanguageServerException;

/**
 * @author Anatoliy Bazko
 */
public interface LanguageServerFactory {

    /**
     * Starts {@link io.typefox.lsapi.services.LanguageServer}.
     */
    LanguageServer create(String projectPath) throws LanguageServerException;

    /**
     * Gets supported languages.
     */
    LanguageDescription getLanguageDescription();
}
