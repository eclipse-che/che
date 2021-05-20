/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
