/*
 * *****************************************************************************
 *  Copyright (c) 2012-2016 Codenvy, S.A.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Codenvy, S.A. - initial API and implementation
 * *****************************************************************************
 */

package org.eclipse.che.plugin.languageserver.server.registry;

import io.typefox.lsapi.services.LanguageServer;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.languageserver.server.factory.LanguageServerFactory;

/**
 * Is responsible to start new {@link LanguageServer}.
 *
 * @author Anatoliy Bazko
 */
public interface ServerInitializer extends ServerInitializerObservable {
    /**
     * Initialize {@link LanguageServer} with given project path.
     */
    @Nullable
    LanguageServer initialize(LanguageServerFactory factory, String projectPath);
}
