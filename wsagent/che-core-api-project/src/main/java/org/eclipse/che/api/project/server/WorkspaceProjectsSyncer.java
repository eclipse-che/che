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
package org.eclipse.che.api.project.server;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.project.ProjectConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Synchronizer for Project Configurations stored in Workspace Configuration with Agent's state
 *
 * @author gazarenkov
 */
public abstract class WorkspaceProjectsSyncer {

    /**
     * Synchronizes Project Config state on Agent and Master
     * @param projectRegistry project registry
     * @throws ServerException
     */
    public final void sync(ProjectRegistry projectRegistry) throws ServerException {

        List<? extends ProjectConfig> remote = getProjects();

        // check on removed
        List <ProjectConfig> removed = new ArrayList<>();
        for(ProjectConfig r  : remote) {
            if(projectRegistry.getProject(r.getPath()) == null)
                removed.add(r);
        }

        for(ProjectConfig r : removed)
            removeProject(r);


        // update or add
        for(RegisteredProject project : projectRegistry.getProjects()) {

            if(!project.isSynced() && !project.isDetected()) {

                final ProjectConfig config = new NewProjectConfigImpl(project.getPath(),
                                                                      project.getType(),
                                                                      project.getMixins(),
                                                                      project.getName(),
                                                                      project.getDescription(),
                                                                      project.getPersistableAttributes(),
                                                                      null,
                                                                      project.getSource());

                boolean found = false;
                for(ProjectConfig r  : remote) {
                    if(r.getPath().equals(project.getPath())) {
                        updateProject(config);
                        found = true;
                    }
                }

                if(!found)
                    addProject(config);

                project.setSync();

            }


        }

    }

    /**
     * @return projects from Workspace Config
     * @throws ServerException
     */
    public abstract List<? extends ProjectConfig> getProjects() throws ServerException;

    /**
     * @return workspace ID
     */
    public abstract String getWorkspaceId();

    /**
     * Adds project to Workspace Config
     * @param project the project config
     * @throws ServerException
     */
    protected abstract void addProject(ProjectConfig project) throws ServerException;

    /**
     * Updates particular project in Workspace Config
     * @param project the project config
     * @throws ServerException
     */
    protected abstract void updateProject(ProjectConfig project) throws ServerException;

    /**
     * Removes particular project in Workspace Config
     * @param project the project config
     * @throws ServerException
     */
    protected abstract void removeProject(ProjectConfig project) throws ServerException;

}
