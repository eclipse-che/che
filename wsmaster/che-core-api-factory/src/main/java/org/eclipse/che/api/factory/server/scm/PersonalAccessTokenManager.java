/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
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

import java.util.Optional;
import org.eclipse.che.api.factory.server.scm.exception.ScmCommunicationException;
import org.eclipse.che.api.factory.server.scm.exception.ScmConfigurationPersistenceException;
import org.eclipse.che.api.factory.server.scm.exception.ScmUnauthorizedException;
import org.eclipse.che.api.factory.server.scm.exception.UnknownScmProviderException;
import org.eclipse.che.api.factory.server.scm.exception.UnsatisfiedScmPreconditionException;
import org.eclipse.che.commons.subject.Subject;

/** Manages {@link PersonalAccessToken}s in Che's permanent storage. */
public interface PersonalAccessTokenManager {
  /**
   * Fetches a new {@link PersonalAccessToken} token from scm provider and save it in permanent
   * storage for further usage.
   *
   * @param cheUser
   * @param scmServerUrl
   * @return personal access token
   * @throws UnsatisfiedScmPreconditionException - storage preconditions aren't met.
   * @throws ScmConfigurationPersistenceException - problem occurred during communication with
   *     permanent storage.
   * @throws ScmUnauthorizedException - scm authorization required.
   * @throws ScmCommunicationException - problem occurred during communication with scm provider.
   * @throws UnknownScmProviderException - scm provider is unknown.
   */
  PersonalAccessToken fetchAndSave(Subject cheUser, String scmServerUrl)
      throws UnsatisfiedScmPreconditionException, ScmConfigurationPersistenceException,
          ScmUnauthorizedException, ScmCommunicationException, UnknownScmProviderException;

  /**
   * Gets {@link PersonalAccessToken} from permanent storage.
   *
   * @param cheUser
   * @param scmServerUrl
   * @return personal access token
   * @throws ScmConfigurationPersistenceException - problem occurred during communication with *
   *     permanent storage.
   */
  Optional<PersonalAccessToken> get(Subject cheUser, String scmServerUrl)
      throws ScmConfigurationPersistenceException, ScmUnauthorizedException,
          ScmCommunicationException;
}
