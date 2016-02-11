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

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.handlers.ProjectCreatedHandler;
import org.eclipse.che.ide.extension.maven.server.projecttype.MavenClassPathConfigurator;
import org.eclipse.che.ide.extension.maven.shared.MavenAttributes;

/**
 * @author Roman Nikitenko
 */
public class MavenProjectCreatedHandler implements ProjectCreatedHandler {

    @Override
    public void onProjectCreated(FolderEntry projectFolder)
            throws ServerException, ForbiddenException, ConflictException, NotFoundException {
        MavenClassPathConfigurator.configure(projectFolder);
    }

    @Override
    public String getProjectType() {
        return MavenAttributes.MAVEN_ID;
    }
}
