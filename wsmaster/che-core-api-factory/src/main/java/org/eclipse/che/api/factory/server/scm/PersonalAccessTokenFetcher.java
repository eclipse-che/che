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
package org.eclipse.che.api.factory.server.scm;

import org.eclipse.che.api.factory.server.scm.exception.ScmCommunicationException;
import org.eclipse.che.api.factory.server.scm.exception.ScmUnauthorizedException;

public interface PersonalAccessTokenFetcher {
  /**
   * Retrieve new PersonalAccessToken from concrete scm provider
   *
   * @param cheUserId
   * @param scmServerUrl
   * @return - personal access token.
   * @throws ScmUnauthorizedException - in case if user are not authorized che server to create new
   *     token. Further user interaction is needed before calling next time this method.
   * @throws ScmCommunicationException - Some unexpected problem occurred during communication with
   *     scm provider.
   */
  PersonalAccessToken fetchPersonalAccessToken(String cheUserId, String scmServerUrl)
      throws ScmUnauthorizedException, ScmCommunicationException;
}
