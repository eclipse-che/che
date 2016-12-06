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
package org.eclipse.che.plugin.java.plain.server.rest;

import com.google.inject.Inject;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.project.NewProjectConfig;
import org.eclipse.che.api.project.server.NewProjectConfigImpl;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.ProjectRegistry;
import org.eclipse.che.api.project.server.RegisteredProject;
import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntryDto;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.eclipse.core.runtime.Path.fromOSString;
import static org.eclipse.jdt.core.JavaCore.newContainerEntry;
import static org.eclipse.jdt.core.JavaCore.newLibraryEntry;
import static org.eclipse.jdt.core.JavaCore.newProjectEntry;
import static org.eclipse.jdt.core.JavaCore.newSourceEntry;
import static org.eclipse.jdt.core.JavaCore.newVariableEntry;

/**
 * Service for updating classpath.
 *
 * @author Valeriy Svydenko
 */
@Path("jdt/classpath/update")
public class ClasspathUpdaterService {
    private static final JavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();

    private final ProjectManager  projectManager;
    private final ProjectRegistry projectRegistry;

    @Inject
    public ClasspathUpdaterService(ProjectManager projectManager, ProjectRegistry projectRegistry) {
        this.projectManager = projectManager;
        this.projectRegistry = projectRegistry;
    }

    /**
     * Updates the information about classpath.
     *
     * @param projectPath
     *         path to the current project
     * @param entries
     *         list of classpath entries which need to set
     * @throws JavaModelException
     *         if JavaModel has a failure
     * @throws ServerException
     *         if some server error
     * @throws ForbiddenException
     *         if operation is forbidden
     * @throws ConflictException
     *         if update operation causes conflicts
     * @throws NotFoundException
     *         if Project with specified path doesn't exist in workspace
     * @throws IOException
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateClasspath(@QueryParam("projectpath") String projectPath, List<ClasspathEntryDto> entries) throws JavaModelException,
                                                                                                                       ServerException,
                                                                                                                       ForbiddenException,
                                                                                                                       ConflictException,
                                                                                                                       NotFoundException,
                                                                                                                       IOException {
        IJavaProject javaProject = model.getJavaProject(projectPath);

        javaProject.setRawClasspath(createModifiedEntry(entries), javaProject.getOutputLocation(), new NullProgressMonitor());

        updateProjectConfig(projectPath);
    }

    private void updateProjectConfig(String projectPath) throws IOException,
                                                                ForbiddenException,
                                                                ConflictException,
                                                                NotFoundException,
                                                                ServerException {
        RegisteredProject project = projectRegistry.getProject(projectPath);

        NewProjectConfig projectConfig = new NewProjectConfigImpl(projectPath,
                                                              project.getName(),
                                                              project.getType(),
                                                              project.getSource());
        projectManager.updateProject(projectConfig);
    }

    private IClasspathEntry[] createModifiedEntry(List<ClasspathEntryDto> entries) {
        List<IClasspathEntry> coreClasspathEntries = new ArrayList<>(entries.size());
        for (ClasspathEntryDto entry : entries) {
            IPath path = fromOSString(entry.getPath());
            int entryKind = entry.getEntryKind();
            if (IClasspathEntry.CPE_LIBRARY == entryKind) {
                coreClasspathEntries.add(newLibraryEntry(path, null, null));
            } else if (IClasspathEntry.CPE_SOURCE == entryKind) {
                coreClasspathEntries.add(newSourceEntry(path));
            } else if (IClasspathEntry.CPE_VARIABLE == entryKind) {
                coreClasspathEntries.add(newVariableEntry(path, null, null));
            } else if (IClasspathEntry.CPE_CONTAINER == entryKind) {
                coreClasspathEntries.add(newContainerEntry(path));
            } else if (IClasspathEntry.CPE_PROJECT == entryKind) {
                coreClasspathEntries.add(newProjectEntry(path));
            }
        }
        return coreClasspathEntries.toArray(new IClasspathEntry[coreClasspathEntries.size()]);
    }
}
