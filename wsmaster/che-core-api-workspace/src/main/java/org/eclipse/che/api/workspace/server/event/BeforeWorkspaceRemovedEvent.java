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
package org.eclipse.che.api.workspace.server.event;

import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.core.db.cascade.event.RemoveEvent;

/**
 * Published before {@link WorkspaceImpl workspace} removed.
 *
 * @author Yevhenii Voevodin
 */
public class BeforeWorkspaceRemovedEvent extends RemoveEvent {

  private final WorkspaceImpl workspace;

  public BeforeWorkspaceRemovedEvent(WorkspaceImpl workspace) {
    this.workspace = workspace;
  }

  public WorkspaceImpl getWorkspace() {
    return workspace;
  }
}
