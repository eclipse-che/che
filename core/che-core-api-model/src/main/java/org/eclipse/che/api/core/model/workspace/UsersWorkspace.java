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
package org.eclipse.che.api.core.model.workspace;

/**
 * Defines workspace owned by user.
 *
 * @author gazarenkov
 */
public interface UsersWorkspace {
    /**
     * Configuration used to create this workspace
     */
    WorkspaceConfig getConfig();

    /**
     * Returns workspace identifier. It is unique and mandatory.
     */
    String getId();

    /**
     * Returns workspace owner (users identifier). It is mandatory.
     */
    String getOwner();

    /**
     * Returns true if workspace is temporary otherwise returns false.
     */
    boolean isTemporary();

    /**
     * Returns workspace status.
     */
    WorkspaceStatus getStatus();
}
