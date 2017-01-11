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
package org.eclipse.che.plugin.maven.server.core;

import org.eclipse.che.plugin.maven.server.core.project.MavenProject;
import org.eclipse.core.resources.IProject;

/**
 * @author Evgen Vidolob
 */
public class MavenProjectResolveTask implements MavenProjectTask {

    private final MavenProject        mavenProject;
    private final MavenProjectManager projectManager;
    private final Runnable            afterTask;

    public MavenProjectResolveTask(MavenProject mavenProject, MavenProjectManager projectManager, Runnable afterTask) {
        this.mavenProject = mavenProject;
        this.projectManager = projectManager;
        this.afterTask = afterTask;
    }

    @Override
    public void perform() {
        IProject project = mavenProject.getProject();
        if (!project.exists()) {
            return;
        }
        projectManager.resolveMavenProject(project, mavenProject);
        if (afterTask != null) {
            afterTask.run();
        }

    }
}
