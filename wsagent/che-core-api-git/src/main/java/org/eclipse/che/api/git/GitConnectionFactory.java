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
package org.eclipse.che.api.git;

import java.io.File;
import org.eclipse.che.api.core.util.LineConsumerFactory;
import org.eclipse.che.api.git.exception.GitException;

/** @author andrew00x */
public abstract class GitConnectionFactory {
  /**
   * Get connection to Git repository located in <code>workDir</code>
   *
   * @param workDir repository directory
   * @return connection to Git repository
   * @throws GitException if can't initialize connection
   */
  public final GitConnection getConnection(String workDir) throws GitException {
    return getConnection(new File(workDir));
  }

  /**
   * Get connection to Git repository located in <code>workDir</code>
   *
   * @param workDir repository directory
   * @param outputPublisherFactory a factory consumer for git output
   * @return connection to Git repository
   * @throws GitException if can't initialize connection
   */
  public final GitConnection getConnection(
      String workDir, LineConsumerFactory outputPublisherFactory) throws GitException {
    return getConnection(new File(workDir), outputPublisherFactory);
  }

  /**
   * Get connection to Git repository located in <code>workDir</code>
   *
   * @param workDir repository directory
   * @return connection to Git repository
   * @throws GitException if can't initialize connection
   */
  public final GitConnection getConnection(File workDir) throws GitException {
    return getConnection(workDir, LineConsumerFactory.NULL);
  }

  /**
   * Get connection to Git repository locate in <code>workDir</code>
   *
   * @param workDir repository directory
   * @param outputPublisherFactory to create a consumer for git output
   * @return connection to Git repository
   * @throws GitException if can't initialize connection
   */
  public abstract GitConnection getConnection(
      File workDir, LineConsumerFactory outputPublisherFactory) throws GitException;

  public abstract CredentialsLoader getCredentialsLoader();
}
