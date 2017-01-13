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
package org.eclipse.che.api.workspace.server;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Workspace;

import java.io.IOException;

/**
 * This component removes workspace files with user's projects after delete workspace operation.
 *
 * @author Alexander Andrienko
 */
public interface WorkspaceFilesCleaner {

    /**
     * Removes workspace files with all projects. Note: all projects data for {@code workspace} will be lost.
     *
     * @param workspace
     *         workspace to clean up files
     */
    void clear(Workspace workspace) throws IOException, ServerException;
}
