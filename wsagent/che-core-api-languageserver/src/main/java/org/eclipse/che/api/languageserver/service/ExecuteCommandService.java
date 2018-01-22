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
package org.eclipse.che.api.languageserver.service;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcException;
import org.eclipse.che.api.languageserver.exception.LanguageServerException;
import org.eclipse.che.api.languageserver.registry.InitializedLanguageServer;
import org.eclipse.che.api.languageserver.registry.LanguageServerRegistry;
import org.eclipse.che.api.languageserver.shared.model.ExtendedExecuteCommandParams;
import org.eclipse.che.api.languageserver.util.LSOperation;
import org.eclipse.che.api.languageserver.util.OperationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Json RPC API for the textDoc to send workspace/executeCommand requests. */
@Singleton
public class ExecuteCommandService {
  private static final Logger LOG = LoggerFactory.getLogger(ExecuteCommandService.class);

  private LanguageServerRegistry registry;

  @Inject
  public ExecuteCommandService(LanguageServerRegistry registry) {
    this.registry = registry;
  }

  /**
   * Sends a workspace/executeCommand request to the applicable language servers with custom
   * command.
   *
   * @param extendedExecuteCommandParams command parameters describe custom command {@link
   *     ExtendedExecuteCommandParams}
   * @return result of executing workspace/executeCommand
   */
  public Object executeCommand(ExtendedExecuteCommandParams extendedExecuteCommandParams) {

    final Object[] result = new Object[1];
    try {
      List<InitializedLanguageServer> servers =
          registry
              .getApplicableLanguageServers(extendedExecuteCommandParams.getFileUri())
              .stream()
              .flatMap(Collection::stream)
              .collect(Collectors.toList());
      OperationUtil.doInSequence(
          servers,
          new LSOperation<InitializedLanguageServer, Object>() {
            @Override
            public boolean canDo(InitializedLanguageServer element) {
              return element.getServer().getWorkspaceService() != null;
            }

            @Override
            public CompletableFuture<Object> start(InitializedLanguageServer element) {
              return element
                  .getServer()
                  .getWorkspaceService()
                  .executeCommand(extendedExecuteCommandParams);
            }

            @Override
            public boolean handleResult(InitializedLanguageServer element, Object response) {
              result[0] = response;
              return true;
            }
          },
          10_000);
      return result[0];
    } catch (LanguageServerException e) {
      LOG.error("error executing command", e);
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }
}
