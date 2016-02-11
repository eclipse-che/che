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
import org.eclipse.che.commons.env.EnvironmentContext;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;

/**
 * @author andrew00x
 */
@Singleton
public class MachineFSMountStrategy implements LocalFSMountStrategy {
    public final String mountProjectsRoot;

    @Inject
    public MachineFSMountStrategy(@Named("che.user.workspaces.storage") String mountProjectsRoot) {
        this.mountProjectsRoot = mountProjectsRoot;
    }

    @Override
    public File getMountPath(String workspaceId) throws ServerException {
        return new File(mountProjectsRoot);
    }

    @Override
    public File getMountPath() throws ServerException {
        return getMountPath(EnvironmentContext.getCurrent().getWorkspaceId());
    }
}

