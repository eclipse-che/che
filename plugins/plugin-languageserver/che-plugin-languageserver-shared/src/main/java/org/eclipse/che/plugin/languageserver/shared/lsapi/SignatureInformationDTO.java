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

import io.typefox.lsapi.SignatureInformation;

@DTO
public interface SignatureInformationDTO extends SignatureInformation {
    /**
     * The label of this signature. Will be shown in the UI.
     * 
     */
    public abstract void setLabel(final String label);

    /**
     * The human-readable doc-comment of this signature. Will be shown in the UI
     * but can be omitted.
     * 
     */
    public abstract void setDocumentation(final String documentation);

    /**
     * The parameters of this signature. Overridden to return the DTO type.
     * 
     */
    public abstract List<ParameterInformationDTO> getParameters();

    /**
     * The parameters of this signature.
     * 
     */
    public abstract void setParameters(final List<ParameterInformationDTO> parameters);
}
