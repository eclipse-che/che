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
package org.eclipse.che.api.git;

import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.shared.ProviderInfo;

/**
 * Provides credentials to use with git commands that need it
 *
 * @author Eugene Voevodin
 * @author Sergii Kabashniuk
 */
public interface CredentialsProvider {
  /**
   * @return credentials for current user in this provider to execute git operation.
   * @throws GitException
   */
  UserCredential getUserCredential() throws GitException;

  /** @return Provider id. */
  String getId();

  /**
   * @param url
   * @return return true if current provider can provide credentials for the given url.
   */
  boolean canProvideCredentials(String url);

  /** @return additional information about given provider */
  ProviderInfo getProviderInfo();
}
