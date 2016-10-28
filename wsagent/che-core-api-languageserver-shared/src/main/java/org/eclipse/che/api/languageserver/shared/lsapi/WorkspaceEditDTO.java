/**
 * Copyright (c) 2016 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.che.api.languageserver.shared.lsapi;

import io.typefox.lsapi.WorkspaceEdit;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;
import java.util.Map;

/**
 * @author Sven Efftinge
 */
@DTO
public interface WorkspaceEditDTO extends WorkspaceEdit {
    /**
     * Holds changes to existing resources.
     * Overridden to return the DTO type.
     */
    Map<String, List<TextEditDTO>> getChanges();
    
    /**
     * Holds changes to existing resources.
     */
    void setChanges(final Map<String, List<TextEditDTO>> changes);
}
