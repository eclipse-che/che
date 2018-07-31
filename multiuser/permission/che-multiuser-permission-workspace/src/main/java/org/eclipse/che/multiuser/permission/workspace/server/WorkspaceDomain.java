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
package org.eclipse.che.multiuser.permission.workspace.server;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.eclipse.che.multiuser.api.permission.server.AbstractPermissionsDomain;
import org.eclipse.che.multiuser.permission.workspace.server.model.impl.WorkerImpl;

/**
 * Domain for storing workspaces' permissions
 *
 * @author Sergii Leschenko
 */
public class WorkspaceDomain extends AbstractPermissionsDomain<WorkerImpl> {
  public static final String READ = "read";
  public static final String RUN = "run";
  public static final String USE = "use";
  public static final String CONFIGURE = "configure";
  public static final String DELETE = "delete";

  public static final String DOMAIN_ID = "workspace";

  public WorkspaceDomain() {
    super(DOMAIN_ID, ImmutableList.of(READ, RUN, USE, CONFIGURE, DELETE));
  }

  @Override
  public WorkerImpl doCreateInstance(
      String userId, String instanceId, List<String> allowedActions) {
    return new WorkerImpl(instanceId, userId, allowedActions);
  }
}
