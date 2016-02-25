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

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.project.server.type.InvalidValueException;
import org.eclipse.che.api.project.server.type.ProjectTypeConstraintException;
import org.eclipse.che.api.project.server.type.ValueStorageException;

import java.util.List;

/**
 * @author Vitalii Parfonov
 */
public interface ProjectRegistry {

    String getWorkspaceId();

    /**
     * @return all the projects
     * @throws ServerException
     *         if projects are not initialized yet
     */
    List<RegisteredProject> getProjects() throws ServerException;

    /**
     * @param projectPath
     * @return project or null if not found
     * @throws ServerException
     *         if projects are not initialized yet
     */
    RegisteredProject getProject(String projectPath) throws ServerException;

    /**
     * @param parentPath
     *         where to find
     * @return child projects
     * @throws ServerException
     *         if projects are not initialized yet
     */
    List<String> getProjects(String parentPath) throws ServerException;

    /**
     * @param path
     * @return the project owned this path.
     * @throws NotFoundException
     *         if not such a project found
     * @throws ServerException
     *         if projects are not initialized yet
     */
    RegisteredProject getParentProject(String path) throws NotFoundException, ServerException;

    RegisteredProject putProject(ProjectConfig config, FolderEntry folder, boolean updated, boolean detected) throws ServerException,
                                                                                                                     ConflictException,
                                                                                                                     NotFoundException,
                                                                                                                     ForbiddenException;

    /**
     * To init new project from sources
     *
     * @param projectPath
     * @param type
     * @return
     * @throws ProjectTypeConstraintException
     * @throws InvalidValueException
     * @throws ValueStorageException
     * @throws NotFoundException
     * @throws ServerException
     */
    RegisteredProject initProject(String projectPath, String type) throws ConflictException,
                                                                          ForbiddenException,
                                                                          NotFoundException,
                                                                          ServerException;

    /**
     * To reinit parent project
     *
     * @param ofPath
     * @return
     * @throws ProjectTypeConstraintException
     * @throws InvalidValueException
     * @throws ValueStorageException
     * @throws NotFoundException
     * @throws ServerException
     */
    RegisteredProject reinitParentProject(String ofPath) throws ConflictException, ForbiddenException, NotFoundException, ServerException;

    /**
     * removes all projects on and under the incoming path
     *
     * @param path
     * @throws ServerException
     *         if projects are not initialized yet
     */
    void removeProjects(String path) throws ServerException;
}
