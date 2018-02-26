/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
            "Runtime start for identity 'workspace: %s, environment: %s, owner: %s' is interrupted",
            identity.getWorkspaceId(), identity.getEnvName(), identity.getOwnerName()));
  }
}
