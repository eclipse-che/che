/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.statepersistance.dto;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * DTO describes saved state of the workspace.
 *
 * @author Artem Zatsarynnyi
 */
@DTO
public interface WorkspaceState {

    /** Returns the list of the actions that should be performed in order to restore workspace's state. */
    List<ActionDescriptor> getActions();

    /**
     * Sets the list of the actions that should be performed in order to restore workspace's state.
     *
     * @param actions
     *         the list of the actions
     */
    void setActions(List<ActionDescriptor> actions);
}
