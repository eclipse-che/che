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
package org.eclipse.che.api.account.shared.dto;

import org.eclipse.che.dto.shared.DTO;


/**
 * @author Ann Shumilova
 */
@DTO
public interface WorkspaceLockDetails {
    String getWorkspaceId();

    void setWorkspaceId(String accountId);

    WorkspaceLockDetails withWorkspaceId(String workspaceId);

    Boolean isLocked();

    void setLocked(Boolean isLocked);

    WorkspaceLockDetails withLocked(Boolean isLocked);
}
