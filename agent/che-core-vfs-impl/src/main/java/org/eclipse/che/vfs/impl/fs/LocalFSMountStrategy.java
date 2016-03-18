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
package org.eclipse.che.vfs.impl.fs;

import org.eclipse.che.api.core.ServerException;

import java.io.IOException;

/**
 * Get local directory for 'mounting' virtual filesystem.
 *
 * @author andrew00x
 */
public interface LocalFSMountStrategy {
    /**
     * Get 'mount point' for specified {@code workspaceId}.
     *
     * @param workspaceId
     *         id of workspace
     * @return location on local file system where virtual filesystem should be mounter
     * @throws ServerException
     */
    java.io.File getMountPath(String workspaceId) throws ServerException;

    /** Get 'mount point' for current workspace. Current workspace may be obtained in implementation specific way from existed context. */
    java.io.File getMountPath() throws ServerException;
}
