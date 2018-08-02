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
package org.eclipse.che.selenium.core.client;

/** @author Musienko Maxim */
public interface TestMachineServiceClient {

  /**
   * Returns machine token for current workspace
   *
   * @param authToken the authorization token
   * @param workspaceId the workspace id
   * @return the machine token for current workspace
   */
  String getMachineApiToken(String workspaceId) throws Exception;
}
