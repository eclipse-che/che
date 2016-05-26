/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.plugin.languageserver.shared.lsapi;

import org.eclipse.che.dto.shared.DTO;

import io.typefox.lsapi.VersionedTextDocumentIdentifier;

@DTO
public interface VersionedTextDocumentIdentifierDTO extends VersionedTextDocumentIdentifier {
    /**
     * The version number of this document.
     * 
     */
    public abstract void setVersion(final int version);

    /**
     * The text document's uri.
     * 
     */
    public abstract void setUri(final String uri);
}
