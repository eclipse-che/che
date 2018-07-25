/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.maven.server.core.classpath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Helper class for build and manage Java project classpath.
 *
 * <p>Inspired by org.eclipse.m2e.jdt.internal.ClasspathDescriptor
 *
 * @author Evgen Vidolob
 */
public class ClasspathHelper {

  private final List<ClasspathEntryHelper> entries = new ArrayList<>();
  private final Map<IPath, ClasspathEntryHelper> defaultEntries = new HashMap<>();

  private final boolean uniquePaths;

  public ClasspathHelper(boolean uniquePaths) {
    this.uniquePaths = uniquePaths;
  }

  public ClasspathHelper(IJavaProject javaProject) throws JavaModelException {
    this(true);
    for (IClasspathEntry classpathEntry : javaProject.getRawClasspath()) {
      if (!classpathEntry.getPath().equals(javaProject.getProject().getFullPath())) {
        ClasspathEntryHelper helper = new ClasspathEntryHelper(classpathEntry);
        entries.add(helper);
        defaultEntries.put(helper.getPath(), helper);
      }
    }
  }

  public ClasspathEntryHelper addSourceEntry(IPath path, IPath outputLocation) {
    ClasspathEntryHelper helper = new ClasspathEntryHelper(path, IClasspathEntry.CPE_SOURCE);
    helper.setOutputLocation(outputLocation);

    ClasspathEntryHelper oldHelper = defaultEntries.get(path);
    if (oldHelper != null) {
      oldHelper.getClasspathAttribute().forEach(helper::setClasspathAttribute);
    }

    addEntryHelper(helper);
    return helper;
  }

  public ClasspathEntryHelper addProjectEntry(IPath projectPath) {
    ClasspathEntryHelper helper =
        new ClasspathEntryHelper(projectPath, IClasspathEntry.CPE_PROJECT);
    addEntryHelper(helper);
    return helper;
  }

  public ClasspathEntryHelper addLibraryEntry(IPath libPath) {
    ClasspathEntryHelper helper = new ClasspathEntryHelper(libPath, IClasspathEntry.CPE_LIBRARY);
    addEntryHelper(helper);
    return helper;
  }

  public ClasspathEntryHelper addContainerEntry(IPath conPath) {
    ClasspathEntryHelper helper = new ClasspathEntryHelper(conPath, IClasspathEntry.CPE_CONTAINER);
    addEntryHelper(helper);
    return helper;
  }

  private void addEntryHelper(ClasspathEntryHelper helper) {
    defaultEntries.remove(helper.getPath());
    ListIterator<ClasspathEntryHelper> iterator = entries.listIterator();
    while (iterator.hasNext()) {
      if (iterator.next().getPath().equals(helper.getPath())) {
        iterator.set(helper);
        return;
      }
    }
    entries.add(helper);
  }

  public IClasspathEntry[] getEntries() {
    return entries
        .stream()
        .map(ClasspathEntryHelper::toClasspathEntry)
        .toArray(IClasspathEntry[]::new);
  }
}
