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
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.dto.server.DtoFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Evgen Vidolob
 */
public class TestWorkspaceHolder extends WorkspaceHolder {
    public TestWorkspaceHolder() throws ServerException {
        super(DtoFactory.newDto(UsersWorkspaceDto.class).withId("id")
                        .withConfig(DtoFactory.newDto(WorkspaceConfigDto.class)
                                              .withName("name")
                                              .withProjects(new ArrayList<>())));
    }

    @Override
    void addProject(RegisteredProject project) throws ServerException {
        if (!project.isDetected()) {
            workspace.addProject(project);
        }
    }

    @Override
    public void updateProject(RegisteredProject project) throws ServerException {
        if (!project.isDetected()) {
            workspace.updateProject(project);
        }
    }

    @Override
    void removeProjects(Collection<RegisteredProject> projects) throws ServerException {
        projects.stream()
                .filter(project -> !project.isDetected())
                .forEach(workspace::removeProject);
    }
}
