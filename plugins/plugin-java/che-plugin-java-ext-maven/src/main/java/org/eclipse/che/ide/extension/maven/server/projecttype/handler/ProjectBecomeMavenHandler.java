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

import com.google.inject.Singleton;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectRegistry;
import org.eclipse.che.api.project.server.handlers.ProjectInitHandler;
import org.eclipse.che.ide.extension.maven.server.projecttype.MavenProjectResolver;

import java.io.IOException;

import static org.eclipse.che.ide.extension.maven.shared.MavenAttributes.MAVEN_ID;

/**
 * @author Vitaly Parfonov
 */
@Singleton
public class ProjectBecomeMavenHandler implements ProjectInitHandler {


    @Override
    public String getProjectType() {
        return MAVEN_ID;
    }

    @Override
    public void onProjectInitialized(ProjectRegistry projectRegistry, FolderEntry projectFolder)
            throws ServerException, ForbiddenException, ConflictException, NotFoundException {

        try {
            MavenProjectResolver.resolve(projectFolder, projectRegistry);
        } catch (IOException e) {
            throw new ServerException(e);
        }

    }
}
