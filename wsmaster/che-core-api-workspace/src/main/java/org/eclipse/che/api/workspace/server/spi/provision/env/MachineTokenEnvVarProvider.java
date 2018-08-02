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
    return Pair.of(
        MACHINE_TOKEN,
        machineTokenProvider.getToken(
            runtimeIdentity.getOwnerId(), runtimeIdentity.getWorkspaceId()));
  }
}
