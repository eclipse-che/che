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
package org.eclipse.che.ide.extension.maven.server.core.classpath;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.eclipse.che.ide.extension.maven.server.core.MavenClasspathContainer;
import org.eclipse.che.ide.extension.maven.server.core.project.MavenProject;
import org.eclipse.che.maven.data.MavenArtifact;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class ClasspathManager {
    public static final String GROUP_ID_ATTRIBUTE    = "maven.groupId";
    public static final String ARTIFACT_ID_ATTRIBUTE = "maven.artifactId";
    public static final String VERSION_ATTRIBUTE     = "maven.version";
    public static final String SCOPE_ATTRIBUTE       = "maven.scope";

    private static final Logger LOG = LoggerFactory.getLogger(ClasspathManager.class);
    private final String workspacePath;

    @Inject
    public ClasspathManager( @Named("che.user.workspaces.storage") String workspacePath) {
        this.workspacePath = workspacePath;

    }

    public void updateClasspath(MavenProject mavenProject) {
        IJavaProject javaProject = JavaCore.create(mavenProject.getProject());
        if (javaProject != null) {
            IClasspathEntry[] entries = getClasspath(javaProject, mavenProject);
            MavenClasspathContainer container = new MavenClasspathContainer(entries);
            try {
                JavaCore.setClasspathContainer(new Path(MavenClasspathContainer.CONTAINER_ID),
                                               new IJavaProject[] {javaProject},
                                               new IClasspathContainer[] {container},
                                               new NullProgressMonitor());
            } catch (JavaModelException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private IClasspathEntry[] getClasspath(IJavaProject javaProject, MavenProject mavenProject) {
        ClasspathHelper helper = new ClasspathHelper(true);

        List<MavenArtifact> dependencies = mavenProject.getDependencies();
        for (MavenArtifact dependency : dependencies) {

            File file = dependency.getFile();
            if (file == null) {
                continue;
            }
            if (file.getPath().endsWith("pom.xml")) {

                String path = file.getParentFile().getPath();
                helper.addProjectEntry(new Path(path.substring(workspacePath.length())));
            } else {
                helper.addLibraryEntry(new Path(file.getPath()));
            }

        }
        //todo add downloaded sources
        return helper.getEntries();
    }
}
