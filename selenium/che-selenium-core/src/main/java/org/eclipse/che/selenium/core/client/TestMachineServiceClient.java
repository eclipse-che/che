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
