/*
 * Copyright (c) 2016-2017 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.composer.server.projecttype;

import static org.eclipse.che.plugin.composer.shared.Constants.COMPOSER_PROJECT_TYPE_ID;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeoutException;
import javax.inject.Inject;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.project.server.handlers.ProjectInitHandler;
import org.eclipse.che.plugin.composer.server.executor.ComposerCommandExecutor;

/** @author Kaloyan Raev */
public class ComposerProjectInitializer implements ProjectInitHandler {

  private ComposerCommandExecutor commandExecutor;
  private PathTransformer pathTransformer;

  @Inject
  public ComposerProjectInitializer(
      ComposerCommandExecutor commandExecutor, PathTransformer pathTransformer) {
    this.commandExecutor = commandExecutor;
    this.pathTransformer = pathTransformer;
  }

  @Override
  public void onProjectInitialized(String projectWsPath)
      throws ServerException, ForbiddenException, ConflictException, NotFoundException {
    String[] commandLine = {"composer", "install"};
    Path path = pathTransformer.transform(projectWsPath);
    try {
      commandExecutor.execute(commandLine, path.toFile());
    } catch (TimeoutException | IOException | InterruptedException e) {
      throw new ServerException(e);
    }
  }

  @Override
  public String getProjectType() {
    return COMPOSER_PROJECT_TYPE_ID;
  }
}
