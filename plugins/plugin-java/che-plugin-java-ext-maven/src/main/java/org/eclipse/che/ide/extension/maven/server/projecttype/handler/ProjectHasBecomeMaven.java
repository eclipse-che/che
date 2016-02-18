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

import com.google.inject.Inject;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.handlers.ProjectTypeChangedHandler;
import org.eclipse.che.ide.extension.maven.server.projecttype.MavenProjectResolver;

import java.io.IOException;

import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.MAVEN_ID;

/**
 * @author Vitaly Parfonov
 */
public class ProjectHasBecomeMaven implements ProjectTypeChangedHandler {

    @Inject
    private ProjectManager projectManager;

    @Inject
    private MavenProjectResolver resolver;

    @Override
    public void onProjectTypeChanged(FolderEntry projectFolder) throws ForbiddenException,
                                                                       ConflictException,
                                                                       ServerException,
                                                                       IOException,
                                                                       NotFoundException {
        resolver.resolve(projectFolder, projectManager);
    }

    @Override
    public String getProjectType() {
        return MAVEN_ID;
    }
}
