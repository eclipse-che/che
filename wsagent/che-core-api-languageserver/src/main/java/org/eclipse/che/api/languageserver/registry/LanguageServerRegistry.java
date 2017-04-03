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

import io.typefox.lsapi.services.LanguageServer;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;
import org.eclipse.che.commons.annotation.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Anatoliy Bazko
 */
public interface LanguageServerRegistry {
    /**
     * Finds appropriate language server according to file name.
     */
    @Nullable
    LanguageServer findServer(String fileUri) throws LanguageServerException;

    /**
     * Returns all available servers.
     */
    Collection<LanguageDescription> getSupportedLanguages();

    /**
     * Returns a map of project-directory-relative paths to server descriptions
     * @return
     */
    Map<String, List<LanguageServerDescription>> getInitializedLanguages();
}
