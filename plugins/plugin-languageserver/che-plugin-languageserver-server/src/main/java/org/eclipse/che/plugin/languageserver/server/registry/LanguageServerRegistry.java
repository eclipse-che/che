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
package org.eclipse.che.plugin.languageserver.server.registry;

import io.typefox.lsapi.InitializeResult;
import io.typefox.lsapi.LanguageDescription;
import io.typefox.lsapi.services.LanguageServer;

import org.eclipse.che.commons.annotation.Nullable;

import java.util.List;

/**
 * @author Anatoliy Bazko
 */
public interface LanguageServerRegistry {
    @Nullable
    LanguageServer findServer(String uri);

    List<LanguageDescription> getSupportedLanguages();

    List<InitializeResult> getRegisteredLanguages();
}
