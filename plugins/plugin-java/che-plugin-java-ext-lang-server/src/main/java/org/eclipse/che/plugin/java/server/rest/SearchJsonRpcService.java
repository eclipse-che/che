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
package org.eclipse.che.plugin.java.server.rest;

import com.google.inject.Inject;
import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcException;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.ide.ext.java.shared.dto.search.FindUsagesRequest;
import org.eclipse.che.ide.ext.java.shared.dto.search.FindUsagesResponse;
import org.eclipse.che.plugin.java.server.search.SearchException;
import org.eclipse.che.plugin.java.server.search.SearchManager;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;

/** Json RPC service for all java project related searches. */
public class SearchJsonRpcService {
  private final SearchManager manager;

  @Inject
  public SearchJsonRpcService(SearchManager manager) {
    this.manager = manager;
  }

  @Inject
  private void configureFindUsagesRequest(RequestHandlerConfigurator requestHandlerConfigurator) {
    requestHandlerConfigurator
        .newConfiguration()
        .methodName("javaSearch/findUsages")
        .paramsAsDto(FindUsagesRequest.class)
        .resultAsDto(FindUsagesResponse.class)
        .withFunction(this::findUsages);
  }

  private FindUsagesResponse findUsages(FindUsagesRequest request) {
    try {
      JavaModel javaModel = JavaModelManager.getJavaModelManager().getJavaModel();
      IJavaProject javaProject = javaModel.getJavaProject(request.getProjectPath());
      return manager.findUsage(javaProject, request.getFQN(), request.getOffset());
    } catch (SearchException e) {
      throw new JsonRpcException(-27000, e.getMessage());
    }
  }
}
