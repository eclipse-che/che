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
package org.eclipse.che.api.workspace.shared.event;

import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.notification.EventOrigin;

/**
 * Informs that workspace was removed.
 *
 * @author Sergii Leschenko
 * @author Alexander Andrienko
 */
@EventOrigin("workspace")
public class WorkspaceRemovedEvent {

  private final Workspace workspace;

  public WorkspaceRemovedEvent(Workspace workspace) {
    this.workspace = workspace;
  }

  public Workspace getWorkspace() {
    return workspace;
  }
}
