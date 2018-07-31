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
package org.eclipse.che.plugin.java.plain.server.generator;

import static java.util.Collections.singletonList;
import static org.eclipse.che.api.fs.server.WsPathUtils.resolve;
import static org.eclipse.che.ide.ext.java.shared.Constants.JAVAC;
import static org.eclipse.che.ide.ext.java.shared.Constants.SOURCE_FOLDER;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.DEFAULT_LIBRARY_FOLDER_VALUE;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.DEFAULT_OUTPUT_FOLDER_VALUE;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.DEFAULT_SOURCE_FOLDER_VALUE;

import com.google.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.plugin.java.plain.server.projecttype.ClasspathBuilder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * Generates new project which contains file with default content.
 *
 * @author Valeriy Svydenko
 */
public class PlainJavaProjectGenerator implements CreateProjectHandler {

  private static final String FILE_NAME = "Main.java";

  private final ClasspathBuilder classpathBuilder;
  private final FsManager fsManager;

  @Inject
  protected PlainJavaProjectGenerator(ClasspathBuilder classpathBuilder, FsManager fsManager) {
    this.classpathBuilder = classpathBuilder;
    this.fsManager = fsManager;
  }

  @Override
  public void onCreateProject(
      String projectWsPath, Map<String, AttributeValue> attributes, Map<String, String> options)
      throws ForbiddenException, ConflictException, ServerException, NotFoundException {

    try (InputStream inputStream =
        getClass().getClassLoader().getResourceAsStream("files/main_class_content")) {
      List<String> sourceFolders;
      if (attributes.containsKey(SOURCE_FOLDER) && !attributes.get(SOURCE_FOLDER).isEmpty()) {
        sourceFolders = attributes.get(SOURCE_FOLDER).getList();
      } else {
        sourceFolders = singletonList(DEFAULT_SOURCE_FOLDER_VALUE);
      }

      fsManager.createDir(projectWsPath);

      String outputDirWsPath = resolve(projectWsPath, DEFAULT_OUTPUT_FOLDER_VALUE);
      fsManager.createDir(outputDirWsPath);

      String sourceDirWsPath = resolve(projectWsPath, sourceFolders.get(0));
      fsManager.createDir(sourceDirWsPath);

      String mainJavaWsPath = resolve(sourceDirWsPath, FILE_NAME);
      fsManager.createFile(mainJavaWsPath, inputStream);

      IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectWsPath);
      IJavaProject javaProject = JavaCore.create(project);

      classpathBuilder.generateClasspath(
          javaProject, sourceFolders, singletonList(DEFAULT_LIBRARY_FOLDER_VALUE));
    } catch (IOException e) {
      throw new ServerException(e);
    }
  }

  @Override
  public String getProjectType() {
    return JAVAC;
  }
}
