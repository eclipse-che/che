/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p>
 * Contributors:
 * Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.project.server;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.project.ProjectConfig;

import java.util.List;

/**
 * //
 *
 * @author Vitalii Parfonov
 */
public interface ProjectRegistry {
    String getWorkspaceId();

    List<RegisteredProject> getProjects() throws ServerException;

    RegisteredProject getProject(String projectPath) throws ServerException;

    List<String> getProjects(String parentPath) throws ServerException;

    RegisteredProject getParentProject(String path) throws NotFoundException, ServerException;

    RegisteredProject putProject(ProjectConfig config, FolderEntry folder, boolean updated, boolean detected)
            throws ServerException, ConflictException,
                   NotFoundException, ForbiddenException;

    RegisteredProject initProject(String projectPath, String type)
            throws ConflictException, ForbiddenException,
                   NotFoundException, ServerException;

    RegisteredProject reinitParentProject(String ofPath)
                           throws ConflictException, ForbiddenException,
                                  NotFoundException, ServerException;

    void removeProjects(String path) throws ServerException;
}
