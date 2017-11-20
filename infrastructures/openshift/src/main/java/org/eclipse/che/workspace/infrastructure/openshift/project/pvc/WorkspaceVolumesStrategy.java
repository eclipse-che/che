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
package org.eclipse.che.workspace.infrastructure.openshift.project.pvc;

import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftEnvironment;

/**
 * Defines a basic set of operations for workspace volume provisioning strategies.
 *
 * @author Anton Korneta
 */
public interface WorkspaceVolumesStrategy {

  /**
   * Prepares volumes for backup of workspace data on a specific machine in a strategy specific way.
   *
   * @param osEnv OpenShift environment that changes as a result of preparation
   * @param workspaceId the workspace identifier for which volumes will be prepared
   * @throws InfrastructureException when any error while preparation occurs
   */
  void prepare(OpenShiftEnvironment osEnv, String workspaceId) throws InfrastructureException;

  /**
   * Cleanups workspace backed up data in a strategy specific way.
   *
   * @param workspaceId the workspace identifier for which cleanup will be performed
   * @throws InfrastructureException when any error while cleanup occurs
   */
  void cleanup(String workspaceId) throws InfrastructureException;
}
