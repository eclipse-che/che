/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.plugin.languageserver.shared.lsapi;

import org.eclipse.che.dto.shared.DTO;

import io.typefox.lsapi.TextDocumentContentChangeEvent;

@DTO
public interface TextDocumentContentChangeEventDTO extends TextDocumentContentChangeEvent {
    /**
     * The range of the document that changed. Overridden to return the DTO
     * type.
     * 
     */
    public abstract RangeDTO getRange();

    /**
     * The range of the document that changed.
     * 
     */
    public abstract void setRange(final RangeDTO range);

    /**
     * The length of the range that got replaced.
     * 
     */
    public abstract void setRangeLength(final Integer rangeLength);

    /**
     * The new text of the document.
     * 
     */
    public abstract void setText(final String text);
}
