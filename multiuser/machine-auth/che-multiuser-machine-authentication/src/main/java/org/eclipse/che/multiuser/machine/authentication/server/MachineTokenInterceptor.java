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
package org.eclipse.che.multiuser.machine.authentication.server;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.commons.env.EnvironmentContext;

/**
 * Intercepts calls to workspace start/stop methods and creates machine authorization token in the
 * registry.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@Singleton
public class MachineTokenInterceptor implements MethodInterceptor {

  @Inject MachineTokenRegistry tokenRegistry;

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    Workspace workspaceToStart = (Workspace) invocation.getArguments()[0];
    // generate machine token for user who starts the workspace
    String workspaceId = workspaceToStart.getId();

    tokenRegistry.generateToken(
        EnvironmentContext.getCurrent().getSubject().getUserId(), workspaceId);
    try {
      return invocation.proceed();
    } catch (Exception e) {
      tokenRegistry.removeTokens(workspaceId);
      throw e;
    }
  }
}
