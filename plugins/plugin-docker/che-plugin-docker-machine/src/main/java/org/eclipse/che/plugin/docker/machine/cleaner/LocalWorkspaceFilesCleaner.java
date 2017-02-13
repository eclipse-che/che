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
package org.eclipse.che.plugin.docker.machine.cleaner;

import com.google.inject.Singleton;

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.workspace.server.WorkspaceFilesCleaner;

import java.io.File;
import java.io.IOException;

import static org.eclipse.che.commons.lang.IoUtil.deleteRecursive;

/**
 * Local implementation of the {@link WorkspaceFilesCleaner}.
 *
 * @author Alexander Andrienko
 * @author Igor Vinokur
 */
@Singleton
public class LocalWorkspaceFilesCleaner implements WorkspaceFilesCleaner {

    @Override
    public void clear(Workspace workspace) throws IOException {
        deleteRecursive(new File("/data/workspaces/" + workspace.getConfig().getName()));
    }
}
