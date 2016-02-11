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
package org.eclipse.che.ide.extension.maven.server.projecttype.handler;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.handlers.GetModulesHandler;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.MAVEN_ID;

/**
 * @author Vitaly Parfonov
 */
public class GetMavenModulesHandler implements GetModulesHandler {

    private final ProjectManager projectManager;

    @Inject
    public GetMavenModulesHandler(ProjectManager projectManager) {
        this.projectManager = projectManager;
    }

    @Override
    public void onGetModules(FolderEntry parentProjectFolder, List<String> modulePaths) throws ForbiddenException, ServerException,
                                                                                               NotFoundException, IOException {
        //TODO: need add checking on module described in parent pom
        List<FolderEntry> childFolders = parentProjectFolder.getChildFolders();
        for (FolderEntry folderEntry : childFolders) {
            if (projectManager.isProjectFolder(folderEntry) && !modulePaths.contains(folderEntry.getPath())) {
                modulePaths.add(folderEntry.getPath());
            }
        }
    }

    @Override
    public String getProjectType() {
        return MAVEN_ID;
    }
}
