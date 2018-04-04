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
package org.eclipse.che.plugin.ssh.key.script;

import org.eclipse.che.api.core.ServerException;

/** @author Sergii Kabashniuk */
public interface SshKeyProvider {

  /**
   * Get private ssh key.
   *
   * @param url url to the repository
   * @return byte array that contains private ssh key
   * @throws ServerException if an error occurs while fetching keys
   */
  byte[] getPrivateKey(String url) throws ServerException;
}
