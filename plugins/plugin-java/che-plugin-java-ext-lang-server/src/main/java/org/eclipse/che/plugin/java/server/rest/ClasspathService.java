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
package org.eclipse.che.plugin.java.server.rest;

import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntryDTO;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Service for getting information about classpath.
 *
 * @author Valeriy Svydenko
 */
@Path("jdt/classpath/")
public class ClasspathService {

    private static final JavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();

    /**
     * Returns information about classpath.
     *
     * @param projectPath
     *         path to the current project
     * @return list of classpath entries
     * @throws JavaModelException
     *         when JavaModel has a failure
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ClasspathEntryDTO> getClasspath(@QueryParam("projectpath") String projectPath) throws JavaModelException {
        IJavaProject javaProject = model.getJavaProject(projectPath);
        IClasspathEntry[] entries = javaProject.getRawClasspath();

        if (entries.length == 0) {
            return emptyList();
        }

        List<ClasspathEntryDTO> entriesDTO = new ArrayList<>(entries.length);
        for (IClasspathEntry entry : entries) {
            ClasspathEntryDTO entryDTO = DtoFactory.getInstance().createDto(ClasspathEntryDTO.class);
            entryDTO.withEntryKind(entry.getEntryKind()).withPath(entry.getPath().toOSString());
            entriesDTO.add(entryDTO);
        }

        return entriesDTO;
    }

}
