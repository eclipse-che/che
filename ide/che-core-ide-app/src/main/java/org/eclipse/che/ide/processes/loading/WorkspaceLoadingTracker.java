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
package org.eclipse.che.ide.processes.loading;

/** Listens workspace events and outputs and visualizes the workspace loading process. */
public interface WorkspaceLoadingTracker {

  /** Starts tracking of workspace loading. */
  void startTracking();

  /** Shows workspace status panel. */
  void showPanel();
}
