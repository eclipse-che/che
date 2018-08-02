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

import org.eclipse.che.ide.api.app.AppContext;

/**
 * Data object to keep cached info about generation ssh keys for {@link AppContext#getCurrentUser()}
 *
 * @author Roman Nikitenko
 */
public class SshKeyGenerationInfo {
  private String host;
  private boolean isSshAvailable;
  private boolean isCanceled;

  public SshKeyGenerationInfo(String host, boolean isSshAvailable, boolean isCanceled) {
    this.host = host;
    this.isSshAvailable = isSshAvailable;
    this.isCanceled = isCanceled;
  }
  /** Gets the host to generate ssh key, such as "github.com" */
  public String getHost() {
    return host;
  }

  /** Sets the host to generate ssh key, such as "github.com" */
  public void setHost(String host) {
    this.host = host;
  }

  /**
   * Gets current user's cached state of ssh keys for the host. Use {@link
   * SshKeyManager#isSshKeyAvailable(String)} to get not cached info
   *
   * @return {@code true} when ssh key is available for the host, {@code false} otherwise
   */
  public boolean isSshAvailable() {
    return isSshAvailable;
  }

  /**
   * Sets current user's state of ssh keys for the host.
   *
   * @param isSshAvailable should be {@code true} when ssh key is available for the host, {@code
   *     false} otherwise
   */
  public void setSshAvailable(boolean isSshAvailable) {
    this.isSshAvailable = isSshAvailable;
  }

  /**
   * Gets info if current user has canceled a prompt to generate ssh key for the host.
   *
   * @return {@code true} when current user has canceled a prompt to generate ssh key for the host,
   *     {@code false} otherwise
   */
  public boolean isCanceled() {
    return isCanceled;
  }

  /**
   * Sets info if current user has canceled a prompt to generate ssh key for the host.
   *
   * @param isCanceled should be {@code true} when current user has canceled a prompt to generate
   *     ssh key for the host, {@code false} otherwise
   */
  public void setCanceled(boolean isCanceled) {
    this.isCanceled = isCanceled;
  }
}
