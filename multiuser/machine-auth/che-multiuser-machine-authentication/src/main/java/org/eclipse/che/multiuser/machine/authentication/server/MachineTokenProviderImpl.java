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
package org.eclipse.che.multiuser.machine.authentication.server;

import static java.lang.String.format;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.workspace.server.token.MachineTokenProvider;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;

/**
 * Provides machine token from {@link MachineTokenRegistry}.
 *
 * <p>Note that {@link MachineTokenRegistry} provides different tokens for different users. Token of
 * current user will be provided for agents.
 *
 * @author Sergii Leshchenko
 */
@Singleton
public class MachineTokenProviderImpl implements MachineTokenProvider {
  private final MachineTokenRegistry tokenRegistry;

  @Inject
  public MachineTokenProviderImpl(MachineTokenRegistry tokenRegistry) {
    this.tokenRegistry = tokenRegistry;
  }

  @Override
  public String getToken(String workspaceId) {
    final Subject subject = EnvironmentContext.getCurrent().getSubject();
    if (subject.isAnonymous()) {
      throw new IllegalStateException(
          format(
              "Unable to get machine token of the workspace '%s' "
                  + "because it does not exist for an anonymous user.",
              workspaceId));
    }
    return getToken(subject.getUserId(), workspaceId);
  }

  @Override
  public String getToken(String userId, String workspaceId) {
    return tokenRegistry.getOrCreateToken(userId, workspaceId);
  }
}
