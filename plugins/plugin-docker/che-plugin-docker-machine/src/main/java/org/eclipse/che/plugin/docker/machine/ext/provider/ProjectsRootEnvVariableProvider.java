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
package org.eclipse.che.plugin.docker.machine.ext.provider;

import org.eclipse.che.plugin.docker.machine.DockerInstanceRuntimeInfo;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Add env variable to docker dev-machine with path to root folder of projects
 *
 * @author Alexander Garagatyi
 */
@Singleton
public class ProjectsRootEnvVariableProvider implements Provider<String> {
    @Inject
    @Named("che.workspace.projects.storage")
    private String projectFolderPath;

    @Override
    public String get() {
        return DockerInstanceRuntimeInfo.PROJECTS_ROOT_VARIABLE + '=' + projectFolderPath;
    }
}
