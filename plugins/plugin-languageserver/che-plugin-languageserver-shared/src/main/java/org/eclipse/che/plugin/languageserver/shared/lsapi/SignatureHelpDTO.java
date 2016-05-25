/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.plugin.languageserver.shared.lsapi;

import java.util.List;

import org.eclipse.che.dto.shared.DTO;

import io.typefox.lsapi.SignatureHelp;

@DTO
public interface SignatureHelpDTO extends SignatureHelp {
    /**
     * One or more signatures. Overridden to return the DTO type.
     * 
     */
    public abstract List<SignatureInformationDTO> getSignatures();

    /**
     * One or more signatures.
     * 
     */
    public abstract void setSignatures(final List<SignatureInformationDTO> signatures);

    /**
     * The active signature.
     * 
     */
    public abstract void setActiveSignature(final Integer activeSignature);

    /**
     * The active parameter of the active signature.
     * 
     */
    public abstract void setActiveParameter(final Integer activeParameter);
}
