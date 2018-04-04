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

import static org.eclipse.che.api.core.util.SystemInfo.isUnix;
import static org.eclipse.che.api.core.util.SystemInfo.isWindows;

import javax.inject.Inject;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.plugin.ssh.key.utils.UrlUtils;

/**
 * Provides SshScript
 *
 * @author Sergii Kabashniuk
 * @author Anton Korneta
 */
public class SshScriptProvider {

  private final SshKeyProvider sshKeyProvider;

  @Inject
  public SshScriptProvider(SshKeyProvider sshKeyProvider) {
    this.sshKeyProvider = sshKeyProvider;
  }

  /**
   * Get SshScript object
   *
   * @param url url to the repository
   * @throws ServerException if an error occurs when creating a script file
   */
  public SshScript getSshScript(String url) throws ServerException {
    String host = UrlUtils.getHost(url);
    if (host == null) {
      throw new ServerException("URL does not have a host");
    }
    if (isWindows()) {
      return new WindowsSshScript(host, sshKeyProvider.getPrivateKey(url));
    }
    if (isUnix()) {
      return new UnixSshScript(host, sshKeyProvider.getPrivateKey(url));
    }
    throw new ServerException("Unsupported OS.");
  }
}
