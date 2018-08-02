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
package org.eclipse.che.api.workspace.server.spi;

import static java.lang.String.format;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;

/**
 * Thrown when start of the runtime is interrupted.
 *
 * @author Anton Korneta
 */
public class RuntimeStartInterruptedException extends InfrastructureException {

  public RuntimeStartInterruptedException(RuntimeIdentity identity) {
    super(
        format(
            "Runtime start for identity 'workspace: %s, environment: %s, ownerId: %s' is interrupted",
            identity.getWorkspaceId(), identity.getEnvName(), identity.getOwnerId()));
  }
}
