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
package org.eclipse.che.plugin.java.plain.server.projecttype;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.api.fs.server.WsPathUtils.absolutize;
import static org.eclipse.che.ide.ext.java.shared.Constants.JAVAC;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.LIBRARY_FOLDER;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.List;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.config.ProjectConfig;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.handlers.ProjectInitHandler;
import org.eclipse.che.plugin.java.languageserver.JavaLanguageServerExtensionService;
import org.eclipse.che.plugin.java.languageserver.JavaLanguageServerLauncher;

/**
 * Init handler for simple java project.
 *
 * @author Evgen Vidolob
 * @author Valeriy Svydenko
 */
public class PlainJavaInitHandler implements ProjectInitHandler {
  private final Provider<JavaLanguageServerExtensionService> extensionService;
  private final Provider<JavaLanguageServerLauncher> languageServerLauncher;
  private final Provider<ProjectManager> projectRegistryProvider;

  @Inject
  public PlainJavaInitHandler(
      Provider<JavaLanguageServerExtensionService> extensionService,
      Provider<JavaLanguageServerLauncher> languageServerLauncher,
      Provider<ProjectManager> projectRegistryProvider) {
    this.extensionService = extensionService;
    this.languageServerLauncher = languageServerLauncher;
    this.projectRegistryProvider = projectRegistryProvider;
  }

  @Override
  public String getProjectType() {
    return JAVAC;
  }

  @Override
  public void onProjectInitialized(String projectFolder)
      throws ServerException, ForbiddenException, ConflictException, NotFoundException {
    if (!languageServerLauncher.get().isStarted()) {
      return;
    }
    String wsPath = absolutize(projectFolder);
    ProjectConfig project =
        projectRegistryProvider
            .get()
            .get(wsPath)
            .orElseThrow(() -> new ServerException("Can't find a project: " + wsPath));

    List<String> library = project.getAttributes().get(LIBRARY_FOLDER);
    if (library != null && !library.isEmpty()) {
      String libraryFolder = library.get(0);
      if (!isNullOrEmpty(libraryFolder)) {
        extensionService.get().addJars(project.getPath(), libraryFolder);
      }
    }
  }
}
