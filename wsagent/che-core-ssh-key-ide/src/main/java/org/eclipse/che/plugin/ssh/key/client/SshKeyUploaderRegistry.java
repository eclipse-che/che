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
package org.eclipse.che.plugin.ssh.key.client;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

/**
 * Holds available ssh key uploaders.
 *
 * @author Sergii Leschenko
 */
@Singleton
public class SshKeyUploaderRegistry {
  private final Map<String, SshKeyUploader> sshKeyUploaders = new HashMap<>();

  /** Get the list of SSH keys uploaders. */
  public Map<String, SshKeyUploader> getUploaders() {
    return new HashMap<>(sshKeyUploaders);
  }

  /** Get the ssh key uploader for given host. */
  public SshKeyUploader getUploader(String host) {
    return sshKeyUploaders.get(host);
  }

  /**
   * Register SSH key uploader
   *
   * @param host host, for which to provide keys
   * @param sshKeyUploader keys uploader
   */
  public void registerUploader(@NotNull String host, @NotNull SshKeyUploader sshKeyUploader) {
    sshKeyUploaders.put(host, sshKeyUploader);
  }
}
