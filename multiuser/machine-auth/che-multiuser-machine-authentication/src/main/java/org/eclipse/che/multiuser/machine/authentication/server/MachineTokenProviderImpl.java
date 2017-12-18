/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.machine.authentication.server;

import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.workspace.server.token.MachineTokenException;
import org.eclipse.che.api.workspace.server.token.MachineTokenProvider;
import org.eclipse.che.commons.env.EnvironmentContext;

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
  public String getToken(String workspaceId) throws MachineTokenException {
    String currentUserId = EnvironmentContext.getCurrent().getSubject().getUserId();
    try {
      return tokenRegistry.getOrCreateToken(currentUserId, workspaceId);
    } catch (NotFoundException e) {
      throw new MachineTokenException(e.getMessage(), e);
    }
  }
}
