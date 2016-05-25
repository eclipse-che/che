/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.plugin.languageserver.shared.lsapi;

import org.eclipse.che.dto.shared.DTO;

import io.typefox.lsapi.DocumentHighlight;

@DTO
public interface DocumentHighlightDTO extends DocumentHighlight {
    /**
     * The range this highlight applies to. Overridden to return the DTO type.
     * 
     */
    public abstract RangeDTO getRange();

    /**
     * The range this highlight applies to.
     * 
     */
    public abstract void setRange(final RangeDTO range);

    /**
     * The highlight kind, default is KIND_TEXT.
     * 
     */
    public abstract void setKind(final Integer kind);
}
