/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.workspace.infrastructure.docker.provisioner.server;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.commons.lang.Pair;

import javax.inject.Inject;
import javax.inject.Named;

import static org.eclipse.che.workspace.infrastructure.docker.DockerMachine.PROJECTS_ROOT_VARIABLE;

/**
 * Add env variable to docker machine with path to root folder of projects
 *
 * @author Alexander Garagatyi
 */
public class ProjectsRootEnvVariableProvider implements ServerEnvironmentVariableProvider {
    private String projectFolderPath;

    @Inject
    public ProjectsRootEnvVariableProvider(@Named("che.workspace.projects.storage") String projectFolderPath) {
        this.projectFolderPath = projectFolderPath;
    }

    @Override
    public Pair<String, String> get(RuntimeIdentity runtimeIdentity) {
        return Pair.of(PROJECTS_ROOT_VARIABLE, projectFolderPath);
    }
}
