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
package org.eclipse.che.api.project.server.handlers;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.project.ProjectConfig;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.type.AttributeValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gazarenkov
 */
public interface CreateProjectHandler extends ProjectHandler {

    /**
     * Called when a new project associated with the primary type equal to {@link #getProjectType()} is about to be
     * created.
     * 
     * <p>
     * NOTE: Until {@link #onCreateProject(FolderEntry, Map, Map)} is removed, this method delegates to it by default.
     * 
     * @param baseFolder
     *            The base folder of the project.
     * @param projectConfig
     *            The initial project configuration. This is a safe copy.
     * @param options
     *            Options passed from the client for the handler.
     * @throws ForbiddenException
     * @throws ConflictException
     * @throws ServerException
     */
    default void onCreateProject(FolderEntry baseFolder, ProjectConfig projectConfig, Map<String, String> options)
            throws ForbiddenException, ConflictException, ServerException {
        Map<String, List<String>> configAtts = projectConfig.getAttributes();
        Map<String, AttributeValue> atts = new HashMap<>(configAtts.size(), 1f);
        configAtts.forEach((k, v) -> atts.put(k, new AttributeValue(v)));
        onCreateProject(baseFolder, atts, options);
    }

    /**
     * @deprecated Use {@link #onCreateProject(FolderEntry, ProjectConfig, Map)} instead.
     */
    @Deprecated
    void onCreateProject(FolderEntry baseFolder,
                         Map<String, AttributeValue> attributes,
                         Map <String, String> options) throws ForbiddenException, ConflictException, ServerException;

}
