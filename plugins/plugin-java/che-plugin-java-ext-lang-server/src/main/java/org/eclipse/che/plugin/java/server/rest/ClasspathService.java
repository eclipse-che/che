/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.server.rest;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.ide.ext.java.shared.dto.classpath.ClasspathEntryDto;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;

/**
 * Service for getting information about classpath.
 *
 * @author Valeriy Svydenko
 */
@Path("java/classpath/")
public class ClasspathService implements ClasspathServiceInterface {

  private static final JavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();

  /**
   * Returns information about classpath.
   *
   * @param projectPath path to the current project
   * @return list of classpath entries
   * @throws JavaModelException when JavaModel has a failure
   */
  @Override
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<ClasspathEntryDto> getClasspath(@QueryParam("projectpath") String projectPath)
      throws JavaModelException {
    IJavaProject javaProject = model.getJavaProject(projectPath);
    IClasspathEntry[] entries = javaProject.getRawClasspath();

    if (entries.length == 0) {
      return emptyList();
    }

    return convertClasspathEntriesToDTO(javaProject, entries);
  }

  private List<ClasspathEntryDto> convertClasspathEntriesToDTO(
      IJavaProject javaProject, IClasspathEntry[] entries) throws JavaModelException {
    List<ClasspathEntryDto> entriesDTO = new ArrayList<>(entries.length);
    for (IClasspathEntry entry : entries) {
      ClasspathEntryDto entryDTO = DtoFactory.getInstance().createDto(ClasspathEntryDto.class);
      entryDTO.withEntryKind(entry.getEntryKind()).withPath(entry.getPath().toOSString());
      if (IClasspathEntry.CPE_CONTAINER == entry.getEntryKind()) {

        IClasspathEntry[] subEntries =
            JavaCore.getClasspathContainer(entry.getPath(), javaProject).getClasspathEntries();

        entryDTO.withExpandedEntries(convertClasspathEntriesToDTO(javaProject, subEntries));
      }
      entriesDTO.add(entryDTO);
    }

    return entriesDTO;
  }
}
