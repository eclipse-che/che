/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.api.languageserver.shared.lsapi;

import io.typefox.lsapi.VersionedTextDocumentIdentifier;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Sven Efftinge
 */
@DTO
public interface VersionedTextDocumentIdentifierDTO extends VersionedTextDocumentIdentifier {
    /**
     * The version number of this document.
     */
    void setVersion(final int version);

    /**
     * The text document's uri.
     */
    void setUri(final String uri);
}
