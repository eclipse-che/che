/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.plugin.languageserver.shared.lsapi;

import org.eclipse.che.dto.shared.DTO;

import io.typefox.lsapi.ParameterInformation;

@DTO
public interface ParameterInformationDTO extends ParameterInformation {
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
}
