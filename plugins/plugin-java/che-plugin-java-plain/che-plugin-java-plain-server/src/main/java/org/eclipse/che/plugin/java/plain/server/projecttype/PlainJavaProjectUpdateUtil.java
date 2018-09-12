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

import static org.eclipse.che.api.fs.server.WsPathUtils.absolutize;
import static org.eclipse.che.api.languageserver.LanguageServiceUtils.removePrefixUri;
import static org.eclipse.che.api.languageserver.util.JsonUtil.convertToJson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.impl.NewProjectConfigImpl;
import org.eclipse.che.api.project.shared.NewProjectConfig;
import org.eclipse.che.api.project.shared.RegisteredProject;
import org.eclipse.che.jdt.ls.extension.api.Notifications;
import org.eclipse.che.plugin.java.languageserver.NotifyJsonRpcTransmitter;
import org.eclipse.lsp4j.ExecuteCommandParams;

public class PlainJavaProjectUpdateUtil {
  public static CompletableFuture<Object> updateProjectConfig(
      ProjectManager projectManager, String projectWsPath)
      throws IOException, ForbiddenException, ConflictException, NotFoundException, ServerException,
          BadRequestException {
    String wsPath = absolutize(projectWsPath);
    RegisteredProject project =
        projectManager
            .get(wsPath)
            .orElseThrow(() -> new NotFoundException("Can't find project: " + projectWsPath));

    NewProjectConfig projectConfig =
        new NewProjectConfigImpl(
            projectWsPath, project.getName(), project.getType(), project.getSource());
    RegisteredProject result = projectManager.update(projectConfig);
    return CompletableFuture.completedFuture(result.getPath());
  }

  public static void notifyClientOnProjectUpdate(
      NotifyJsonRpcTransmitter notifyTransmitter, String projectPath) {
    List<Object> parameters = new ArrayList<>();
    parameters.add(removePrefixUri(convertToJson(projectPath).getAsString()));
    notifyTransmitter.sendNotification(
        new ExecuteCommandParams(Notifications.UPDATE_ON_PROJECT_CLASSPATH_CHANGED, parameters));
  }
}
