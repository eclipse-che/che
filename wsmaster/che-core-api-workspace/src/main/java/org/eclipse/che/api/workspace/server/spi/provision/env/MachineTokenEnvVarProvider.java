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
package org.eclipse.che.api.workspace.server.spi.provision.env;

import javax.inject.Inject;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.token.MachineTokenException;
import org.eclipse.che.api.workspace.server.token.MachineTokenProvider;
import org.eclipse.che.commons.lang.Pair;

/**
 * Provides environment variable with a token that should be used by servers in a container to
 * access Che master API.
 *
 * @author Alexander Garagatyi
 * @author Sergii Leshchenko
 */
public class MachineTokenEnvVarProvider implements EnvVarProvider {
  /** Environment variable that will be setup in machines and contains machine token. */
  public static final String MACHINE_TOKEN = "CHE_MACHINE_TOKEN";

  private final MachineTokenProvider machineTokenProvider;

  @Inject
  public MachineTokenEnvVarProvider(MachineTokenProvider machineTokenProvider) {
    this.machineTokenProvider = machineTokenProvider;
  }

  @Override
  public Pair<String, String> get(RuntimeIdentity runtimeIdentity) throws MachineTokenException {
    return Pair.of(MACHINE_TOKEN, machineTokenProvider.getToken(runtimeIdentity.getWorkspaceId()));
  }
}
