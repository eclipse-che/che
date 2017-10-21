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
