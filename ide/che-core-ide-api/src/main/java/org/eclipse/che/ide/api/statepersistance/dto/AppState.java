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

import java.util.Map;

/**
 * DTO describes IDE application's state that may be saved/restored.
 *
 * @author Artem Zatsarynnyi
 */
@DTO
public interface AppState {

    /** Returns recent workspace ID (workspace which was recently used). */
    String getRecentWorkspaceId();

    /**
     * Set recent workspace ID.
     *
     * @param workspaceId
     *         ID of the workspace which was recently used
     */
    void setRecentWorkspaceId(String workspaceId);

    /** Returns the mapping of the workspaces's ID to it's saved state. */
    Map<String, WorkspaceState> getWorkspaces();

    /**
     * Set saved workspaces's state.
     *
     * @param workspaces
     *         mapping of the workspaces's ID to it's saved state
     */
    void setWorkspaces(Map<String, WorkspaceState> workspaces);
}
