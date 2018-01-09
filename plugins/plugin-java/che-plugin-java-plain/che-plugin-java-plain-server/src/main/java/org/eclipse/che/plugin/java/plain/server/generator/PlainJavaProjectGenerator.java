/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.plain.server.generator;

import static java.util.Collections.singletonList;
import static org.eclipse.che.api.fs.server.WsPathUtils.resolve;
import static org.eclipse.che.ide.ext.java.shared.Constants.JAVAC;
import static org.eclipse.che.ide.ext.java.shared.Constants.SOURCE_FOLDER;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.DEFAULT_OUTPUT_FOLDER_VALUE;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.DEFAULT_SOURCE_FOLDER_VALUE;

import com.google.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.notification.PreProjectInitializedEvent;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.plugin.java.languageserver.JavaLanguageServerExtensionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates new project which contains file with default content.
 *
 * @author Valeriy Svydenko
 */
public class PlainJavaProjectGenerator implements CreateProjectHandler {

  private static final Logger LOG = LoggerFactory.getLogger(PlainJavaProjectGenerator.class);

  private static final String FILE_NAME = "Main.java";

  private final FsManager fsManager;

  @Inject
  protected PlainJavaProjectGenerator(
      JavaLanguageServerExtensionService service, EventService eventService, FsManager fsManager) {
    this.fsManager = fsManager;

    eventService.subscribe(
        new EventSubscriber<PreProjectInitializedEvent>() {
          @Override
          public void onEvent(PreProjectInitializedEvent event) {
            onPreProjectInitializedEvent(event);
          }
        });
  }

  @Override
  public void onCreateProject(
      String projectWsPath, Map<String, AttributeValue> attributes, Map<String, String> options)
      throws ForbiddenException, ConflictException, ServerException, NotFoundException {

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
    fsManager.createFile(
        mainJavaWsPath,
        getClass().getClassLoader().getResourceAsStream("files/main_class_content"));

    createClasspath(projectWsPath, sourceFolders.get(0));
    createProjectConfig(projectWsPath);
  }

  private void createProjectConfig(String projectWsPath)
      throws ConflictException, NotFoundException, ServerException {
    InputStream projectIS =
        getClass().getClassLoader().getResourceAsStream("files/project_config_content");
    try {

      String projectConfigTemplate = IOUtils.toString(projectIS);
      String projectConfWsPath = resolve(projectWsPath, ".project");
      String projectConfContent =
          projectConfigTemplate.replaceFirst("project_name", projectWsPath.substring(1));
      fsManager.createFile(projectConfWsPath, projectConfContent);
    } catch (IOException e) {
      throw new ServerException(e.getMessage());
    }
  }

  private void createClasspath(String projectWsPath, String srcFolder)
      throws ConflictException, NotFoundException, ServerException {
    InputStream classpathIS =
        getClass().getClassLoader().getResourceAsStream("files/classpath_content");
    try {
      String classpathContent = IOUtils.toString(classpathIS);
      String classpathWsPath = resolve(projectWsPath, ".classpath");
      String cpContent = classpathContent.replaceFirst("source_folder_value", srcFolder);
      fsManager.createFile(classpathWsPath, cpContent);
    } catch (IOException e) {
      throw new ServerException(e.getMessage());
    }
  }

  private void onPreProjectInitializedEvent(PreProjectInitializedEvent event) {
    ProjectConfig projectConfig = event.getProjectConfig();
    String oldClasspathWsPath = projectConfig.getPath() + "/.che/classpath";
    if (projectConfig.getType().equals(JAVAC) && fsManager.exists(oldClasspathWsPath)) {
      try {
        fsManager.move(oldClasspathWsPath, projectConfig.getPath() + "/.classpath");
        createProjectConfig(projectConfig.getPath());
      } catch (ConflictException | NotFoundException | ServerException e) {
        LOG.error("Can't update project {}", projectConfig.getPath(), e);
      }
    }
  }

  @Override
  public String getProjectType() {
    return JAVAC;
  }
}
