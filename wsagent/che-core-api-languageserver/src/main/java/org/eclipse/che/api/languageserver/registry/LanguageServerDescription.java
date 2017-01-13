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

import io.typefox.lsapi.InitializeResult;

import org.eclipse.che.api.languageserver.shared.model.LanguageDescription;


/**
 * Simple container for {@link InitializeResult} and {@link LanguageDescription}
 *
 * @author Evgen Vidolob
 */
public class LanguageServerDescription {
    private final InitializeResult    initializeResult;
    private final LanguageDescription languageDescription;

    public LanguageServerDescription(InitializeResult initializeResult,
                                     LanguageDescription languageDescription) {
        this.initializeResult = initializeResult;
        this.languageDescription = languageDescription;
    }

    public InitializeResult getInitializeResult() {
        return initializeResult;
    }

    public LanguageDescription getLanguageDescription() {
        return languageDescription;
    }
}
