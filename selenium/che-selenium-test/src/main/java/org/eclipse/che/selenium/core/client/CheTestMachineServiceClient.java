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
package org.eclipse.che.selenium.core.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Musienko Maxim
 * @author Dmytro Nochevnov
 */
@Singleton
public class CheTestMachineServiceClient implements TestMachineServiceClient {

  private final TestWorkspaceServiceClient testWorkspaceServiceClient;

  @Inject
  public CheTestMachineServiceClient(TestWorkspaceServiceClient testWorkspaceServiceClient) {
    this.testWorkspaceServiceClient = testWorkspaceServiceClient;
  }

  /**
   * Returns machine token for current workspace
   *
   * @param workspaceId the workspace id
   * @return the machine token for current workspace
   */
  @Override
  public String getMachineApiToken(String workspaceId) throws Exception {
    return testWorkspaceServiceClient.getById(workspaceId).getRuntime().getMachineToken();
  }
}
