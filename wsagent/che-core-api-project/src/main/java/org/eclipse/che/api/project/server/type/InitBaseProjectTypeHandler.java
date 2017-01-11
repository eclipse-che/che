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
package org.eclipse.che.api.project.server.type;

import com.google.inject.Singleton;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectRegistry;
import org.eclipse.che.api.project.server.RegisteredProject;
import org.eclipse.che.api.project.server.handlers.ProjectInitHandler;

import java.util.List;

/**
 * Set {@link BaseProjectType} for all sub-projects.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class InitBaseProjectTypeHandler implements ProjectInitHandler {

    @Override
    public String getProjectType() {
        return BaseProjectType.ID;
    }

    @Override
    public void onProjectInitialized(ProjectRegistry projectRegistry, FolderEntry projectFolder) throws ServerException,
                                                                                                        ForbiddenException,
                                                                                                        ConflictException,
                                                                                                        NotFoundException {
        List<String> projects = projectRegistry.getProjects(projectFolder.getPath().toString());
        for (String project : projects) {
            RegisteredProject detected = projectRegistry.getProject(project);
            if (detected.isDetected()) {
                projectRegistry.setProjectType(project, BaseProjectType.ID, false);
            }
        }
    }
}
