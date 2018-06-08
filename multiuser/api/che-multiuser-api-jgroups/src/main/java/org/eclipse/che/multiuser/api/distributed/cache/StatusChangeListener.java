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
package org.eclipse.che.multiuser.api.distributed.cache;

import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;

/**
 * Listener interface for being notified about workspace status changes in {@link
 * JGroupsWorkspaceStatusCache}.
 *
 * <p>Note that JGroupsWorkspaceStatusCache is distributed cache and listener will receiver changes
 * that are made by this instance or any another is a cluster.
 *
 * @author Sergii Leshchenko
 */
public interface StatusChangeListener {
  void statusChanged(String workspaceId, WorkspaceStatus status);
}
