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
package org.eclipse.che.plugin.java.server.projecttype;

import static org.eclipse.che.api.fs.server.WsPathUtils.absolutize;

import com.google.inject.Inject;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.project.server.handlers.ProjectInitHandler;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * General handler for initializing Java project. Basically all you need, for different Java
 * project,is configuring classpath. For that you need to implement {@link
 * #initializeClasspath(IJavaProject)} Sample of classpath configuration: <br>
 *
 * <pre>
 *     IClasspathEntry jreContainer = JavaCore.newContainerEntry(new
 * Path(JREContainerInitializer.JRE_CONTAINER));
 *     javaProject.setRawClasspath(new IClasspathEntry[]{jreContainer}, null);
 * </pre>
 *
 * This configuration just add classpath container for JRE(rt.jar).
 *
 * @author Evgen Vidolob
 */
public abstract class AbstractJavaInitHandler implements ProjectInitHandler {

  private ResourcesPlugin plugin;

  private PathTransformer pathTransformer;

  @Inject
  void init(ResourcesPlugin plugin, PathTransformer pathTransformer) {
    this.plugin = plugin;
    this.pathTransformer = pathTransformer;
  }

  @Override
  public final void onProjectInitialized(String wsPath)
      throws ServerException, ForbiddenException, ConflictException, NotFoundException {
    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(absolutize(wsPath));
    IJavaProject javaProject = JavaCore.create(project);
    initializeClasspath(javaProject);
  }

  /**
   * Implementation of this method assumed to configure Java project classpath. For configuration
   * create array of {@link org.eclipse.jdt.core.IClasspathEntry} and call {@link
   * IJavaProject#setRawClasspath(IClasspathEntry[], IProgressMonitor)} method. Note: classpath must
   * contain dependencies and path to sources
   *
   * @param javaProject the Java project to classpath initializing
   * @throws ServerException when any error happened
   */
  protected abstract void initializeClasspath(IJavaProject javaProject) throws ServerException;
}
