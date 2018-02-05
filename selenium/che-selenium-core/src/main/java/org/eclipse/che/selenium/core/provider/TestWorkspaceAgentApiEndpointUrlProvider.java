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
package org.eclipse.che.selenium.core.provider;

import java.net.URL;

/** Workspace agent API endpoint */
public interface TestWorkspaceAgentApiEndpointUrlProvider {

  /**
   * get workspace agent API endpoint
   *
   * @param workspaceId
   * @return workspace agent api endpoint URL
   * @throws Exception
   */
  URL get(String workspaceId) throws Exception;
}
