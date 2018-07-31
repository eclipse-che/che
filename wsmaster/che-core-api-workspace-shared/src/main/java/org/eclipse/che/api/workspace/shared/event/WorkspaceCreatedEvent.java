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
package org.eclipse.che.api.workspace.shared.event;

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.notification.EventOrigin;

/**
 * Informs about workspace creation.
 *
 * @author Sergii Leschenko
 */
@EventOrigin("workspace")
public class WorkspaceCreatedEvent {
  private final Workspace workspace;

  public WorkspaceCreatedEvent(Workspace workspace) {
    this.workspace = workspace;
  }

  public Workspace getWorkspace() {
    return workspace;
  }
}
