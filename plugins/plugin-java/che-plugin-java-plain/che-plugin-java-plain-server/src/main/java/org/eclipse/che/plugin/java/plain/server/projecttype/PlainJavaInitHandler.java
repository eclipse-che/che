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
package org.eclipse.che.plugin.java.plain.server.projecttype;

import static org.eclipse.che.api.fs.server.WsPathUtils.absolutize;
import static org.eclipse.che.ide.ext.java.shared.Constants.JAVAC;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.LIBRARY_FOLDER;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.impl.RegisteredProject;
import org.eclipse.che.ide.ext.java.shared.Constants;
import org.eclipse.che.plugin.java.server.projecttype.AbstractJavaInitHandler;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Init handler for simple java project. Initialize classpath with JRE classpath entry container and
 * 'src' source classpath entry.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 */
public class PlainJavaInitHandler extends AbstractJavaInitHandler {

  private static final Logger LOG = LoggerFactory.getLogger(PlainJavaInitHandler.class);
  private final ClasspathBuilder classpathBuilder;
  private final Provider<ProjectManager> projectRegistryProvider;

  @Inject
  public PlainJavaInitHandler(
      ClasspathBuilder classpathBuilder, Provider<ProjectManager> projectRegistryProvider) {
    this.classpathBuilder = classpathBuilder;
    this.projectRegistryProvider = projectRegistryProvider;
  }

  @Override
  protected void initializeClasspath(IJavaProject javaProject) throws ServerException {
    IClasspathEntry[] projectClasspath;
    try {
      projectClasspath = javaProject.getRawClasspath();
    } catch (JavaModelException e) {
      LOG.warn(
          "Can't get classpath for: " + javaProject.getProject().getFullPath().toOSString(), e);
      throw new ServerException(e);
    }

    // default classpath
    IClasspathEntry[] defaultClasspath =
        new IClasspathEntry[] {JavaCore.newSourceEntry(javaProject.getPath())};
    if (!Arrays.equals(defaultClasspath, projectClasspath)) {
      // classpath is already initialized
      return;
    }

    String wsPath = absolutize(javaProject.getPath().toOSString());
    RegisteredProject project =
        projectRegistryProvider
            .get()
            .get(wsPath)
            .orElseThrow(() -> new ServerException("Can't find a project: " + wsPath));

    List<String> sourceFolders = project.getAttributes().get(Constants.SOURCE_FOLDER);
    List<String> library = project.getAttributes().get(LIBRARY_FOLDER);

    classpathBuilder.generateClasspath(javaProject, sourceFolders, library);
  }

  @Override
  public String getProjectType() {
    return JAVAC;
  }
}
