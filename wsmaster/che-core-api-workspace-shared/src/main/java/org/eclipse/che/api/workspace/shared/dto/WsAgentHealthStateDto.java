/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.workspace.shared.dto;

import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.dto.shared.DTO;

/**
 * Describes status of the ws agent.
 *
 * @author Vitalii Parfonov
 * @author Valeriy Svydenko
 */
@DTO
public interface WsAgentHealthStateDto {

    void setWorkspaceStatus(WorkspaceStatus status);

    WsAgentHealthStateDto withWorkspaceStatus(WorkspaceStatus status);

    /**
     * Returns the status of the current workspace instance.
     * <p>
     * <p>All the workspaces which are stopped have runtime
     * are considered {@link WorkspaceStatus#STOPPED}.
     */
    WorkspaceStatus getWorkspaceStatus();

    void setCode(int code);

    /** Returns HTTP status code, see {@code javax.ws.rs.core.Response.Status} */
    int getCode();

    WsAgentHealthStateDto withCode(int code);

    void setReason(String reason);

    /** Returns reason of the state. */
    String getReason();

    WsAgentHealthStateDto withReason(String reason);
}
