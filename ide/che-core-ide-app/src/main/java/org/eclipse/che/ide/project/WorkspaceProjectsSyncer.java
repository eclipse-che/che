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
package org.eclipse.che.ide.project;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.RequestHandlerConfigurator;
import org.eclipse.che.ide.api.app.AppContext;

/** Handles event about workspace updating from the server using JSON RPC. */
@Singleton
public class WorkspaceProjectsSyncer {
  private static final String WORKSPACE_SYNCHRONIZE_METHOD_NAME = "workspace/synchronize";

  private final AppContext appContext;

  @Inject
  public WorkspaceProjectsSyncer(AppContext appContext) {
    this.appContext = appContext;
  }

  @Inject
  private void configureHandlers(RequestHandlerConfigurator configurator) {
    configurator
        .newConfiguration()
        .methodName(WORKSPACE_SYNCHRONIZE_METHOD_NAME)
        .noParams()
        .noResult()
        .withConsumer(this::updateProjects);
  }

  private void updateProjects(String endpointId) {
    // Temporary disabled synchronization due to excessive calls to actual synchronization
    // mechanism.
    // Will be reviewed this mechanism in nearest future. But for now, it temporary disabled.

    //    Container workspaceRoot = appContext.getWorkspaceRoot();
    //    if (workspaceRoot != null) {
    //      workspaceRoot.synchronize();
    //    }
  }
}
