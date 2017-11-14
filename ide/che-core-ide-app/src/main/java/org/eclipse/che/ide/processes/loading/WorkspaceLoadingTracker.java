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
package org.eclipse.che.ide.processes.loading;

/** Listens workspace events and outputs and visualizes the workspace loading process. */
public interface WorkspaceLoadingTracker {

  /** Starts tracking of workspace loading. */
  void startTracking();
}
