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
package io.tonlabs.generator;

import com.google.inject.Inject;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.fs.server.WsPathUtils;
import org.eclipse.che.api.project.server.handlers.CreateProjectHandler;
import org.eclipse.che.api.project.server.type.AttributeValue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static io.tonlabs.shared.Constants.TON_SOL_PROJECT_TYPE_ID;
import static org.eclipse.che.api.fs.server.WsPathUtils.resolve;

/**
 * Generates a new project which contains a package.json with default content and a default
 * person.json file within an myJsonFiles folder.
 */
public class TonSolProjectCreateProjectHandler implements CreateProjectHandler {

  @Inject private FsManager fsManager;

  private static final String FILE_NAME = "hello_world.sol";

  @Override
  public void onCreateProject(
      String projectPath, Map<String, AttributeValue> attributes, Map<String, String> options)
      throws ConflictException, ServerException {

    try (InputStream helloWorld =
        this.getClass().getClassLoader().getResourceAsStream("files/hello_world.sol")) {
      String projectWsPath = WsPathUtils.absolutize(projectPath);
      this.fsManager.createFile(resolve(projectWsPath, FILE_NAME), helloWorld);
    } catch (IOException | NotFoundException e) {
      throw new ServerException(e.getLocalizedMessage(), e);
    }
  }

  @Override
  public String getProjectType() {
    return TON_SOL_PROJECT_TYPE_ID;
  }
}
