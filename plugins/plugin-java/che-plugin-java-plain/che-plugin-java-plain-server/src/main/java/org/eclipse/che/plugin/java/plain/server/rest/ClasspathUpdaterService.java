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
package org.eclipse.che.plugin.java.plain.server.rest;

import static org.eclipse.che.api.fs.server.WsPathUtils.absolutize;
import static org.eclipse.che.api.languageserver.LanguageServiceUtils.prefixURI;
import static org.eclipse.che.api.languageserver.LanguageServiceUtils.removePrefixUri;
import static org.eclipse.che.api.languageserver.util.JsonUtil.convertToJson;

import com.google.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
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
import org.eclipse.che.jdt.ls.extension.api.dto.ClasspathEntry;
import org.eclipse.che.plugin.java.languageserver.JavaLanguageServerExtensionService;
import org.eclipse.che.plugin.java.languageserver.NotifyJsonRpcTransmitter;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for updating classpath.
 *
 * @author Valeriy Svydenko
 */
@Path("jdt/classpath/update")
public class ClasspathUpdaterService {
  private static final Logger LOG = LoggerFactory.getLogger(ClasspathUpdaterService.class);

  private final ProjectManager projectManager;
  private final JavaLanguageServerExtensionService extensionService;
  private final NotifyJsonRpcTransmitter notifyTransmitter;

  @Inject
  public ClasspathUpdaterService(
      ProjectManager projectManager,
      JavaLanguageServerExtensionService extensionService,
      NotifyJsonRpcTransmitter notifyTransmitter) {
    this.projectManager = projectManager;
    this.extensionService = extensionService;
    this.notifyTransmitter = notifyTransmitter;
  }

  /**
   * Updates the information about classpath.
   *
   * @param projectPath path to the current project
   * @param entries list of classpath entries which need to set
   * @throws ServerException if some server error
   * @throws ForbiddenException if operation is forbidden
   * @throws ConflictException if update operation causes conflicts
   * @throws NotFoundException if Project with specified path doesn't exist in workspace
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public void updateClasspath(
      @QueryParam("projectpath") String projectPath, List<ClasspathEntry> entries)
      throws ServerException, ForbiddenException, ConflictException, NotFoundException, IOException,
          BadRequestException {
    try {
      extensionService.updateClasspathWithResult(prefixURI(projectPath), entries).get();
      updateProjectConfig(projectPath).get();
    } catch (InterruptedException | ExecutionException e) {
      LOG.error(e.getMessage());
    }

    notifyClientOnProjectUpdate(projectPath);
  }

  private CompletableFuture<Object> updateProjectConfig(String projectWsPath)
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

  private void notifyClientOnProjectUpdate(String projectPath) {
    List<Object> parameters = new ArrayList<>();
    parameters.add(removePrefixUri(convertToJson(projectPath).getAsString()));
    notifyTransmitter.sendNotification(
        new ExecuteCommandParams(Notifications.UPDATE_ON_PROJECT_CLASSPATH_CHANGED, parameters));
  }
}
