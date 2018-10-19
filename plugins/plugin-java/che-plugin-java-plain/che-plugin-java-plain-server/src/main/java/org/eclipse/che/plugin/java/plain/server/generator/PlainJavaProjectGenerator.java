/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.plain.server.generator;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.eclipse.che.api.fs.server.WsPathUtils.resolve;
import static org.eclipse.che.commons.lang.Deserializer.resolveVariables;
import static org.eclipse.che.ide.ext.java.shared.Constants.JAVAC;
import static org.eclipse.che.ide.ext.java.shared.Constants.SOURCE_FOLDER;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.DEFAULT_OUTPUT_FOLDER_VALUE;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.DEFAULT_SOURCE_FOLDER_VALUE;

import com.google.inject.Inject;
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
import org.eclipse.che.api.project.server.notification.BeforeProjectInitializedEvent;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates new project which contains file with default content.
 *
 * @author Valeriy Svydenko
 */
public class PlainJavaProjectGenerator implements CreateProjectHandler {

  private static final Logger LOG = LoggerFactory.getLogger(PlainJavaProjectGenerator.class);

  private static final String MAIN_CLASS_RESOURCE = "Main.java";
  private static final String PROJECT_FILE_RESOURCE = "project";
  private static final String CLASSPATH_FILE_RESOURCE = "classpath";

  private static final String PROJECT_NAME_TEMPLATE = "project_name";
  private static final String SOURCE_FOLDER_TEMPLATE = "source_folder";

  private static final String CLASSPATH_FILE = ".classpath";
  private static final String PROJECT_FILE = ".project";
  private static final String MAIN_CLASS_FILE = "Main.java";

  private final FsManager fsManager;

  @Inject
  protected PlainJavaProjectGenerator(EventService eventService, FsManager fsManager) {
    this.fsManager = fsManager;

    eventService.subscribe(
        new EventSubscriber<BeforeProjectInitializedEvent>() {
          @Override
          public void onEvent(BeforeProjectInitializedEvent event) {
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

    String mainJavaWsPath = resolve(sourceDirWsPath, MAIN_CLASS_FILE);
    fsManager.createFile(mainJavaWsPath, getResource(MAIN_CLASS_RESOURCE));

    // create .classpath
    String dotClasspathWsPath = resolve(projectWsPath, CLASSPATH_FILE);
    createFile(
        dotClasspathWsPath,
        CLASSPATH_FILE_RESOURCE,
        singletonMap(SOURCE_FOLDER_TEMPLATE, sourceFolders.get(0)));

    // create .project
    String dotProjectWsPath = resolve(projectWsPath, PROJECT_FILE);
    createFile(
        dotProjectWsPath,
        PROJECT_FILE_RESOURCE,
        singletonMap(PROJECT_NAME_TEMPLATE, projectWsPath.substring(1)));
  }

  @Override
  public String getProjectType() {
    return JAVAC;
  }

  private void onPreProjectInitializedEvent(BeforeProjectInitializedEvent event) {
    ProjectConfig projectConfig = event.getProjectConfig();
    String projectWsPath = projectConfig.getPath();
    String oldClasspathWsPath = projectWsPath + "/.che/classpath";
    if (projectConfig.getType().equals(JAVAC) && fsManager.exists(oldClasspathWsPath)) {
      try {
        fsManager.move(oldClasspathWsPath, projectWsPath + "/.classpath");
        createFile(
            resolve(projectWsPath, PROJECT_FILE),
            PROJECT_FILE_RESOURCE,
            singletonMap(PROJECT_NAME_TEMPLATE, projectWsPath.substring(1)));
      } catch (ConflictException | NotFoundException | ServerException e) {
        LOG.error("Can't update project {}", projectWsPath, e);
      }
    }
  }

  private void createFile(String fileWsPath, String resourceName, Map<String, String> parameters)
      throws ConflictException, NotFoundException, ServerException {
    String template = getResource(resourceName);
    String content = resolveVariables(template, parameters);
    fsManager.createFile(fileWsPath, content);
  }

  private String getResource(String resourceName) throws ServerException {
    try (InputStream resourceAsStream = getClass().getResourceAsStream(resourceName)) {
      return IOUtils.toString(resourceAsStream);
    } catch (Exception e) {
      throw new ServerException(e.getMessage());
    }
  }
}
