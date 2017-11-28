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

import com.google.gwt.user.client.ui.IsWidget;

/** Created by vetal on 9/29/17. */
public interface WorkspaceLoadingTrackerView extends IsWidget {

  /** Displays loading title and pulling machines section. */
  void startLoading();

  /**
   * Adds machine to the downloading list.
   *
   * @param machine machine name
   */
  void pullMachine(String machine);

  /**
   * Displays docker image for a machine.
   *
   * @param machine machine name
   * @param image docker image
   */
  void setMachineImage(String machine, String image);

  /**
   * Updates state of machine pulling progress.
   *
   * @param machine machine name
   * @param percents how much of the image has been already downloaded
   */
  void onPullingProgress(String machine, int percents);

  /**
   * Sets pulling complete for machine.
   *
   * @param machine machine name
   */
  void onPullingComplete(String machine);

  /** Switches to step 2. Displays `Starting workspace runtimes` section. */
  void startWorkspaceMachines();

  /**
   * Starts a workspace machine with a docker image.
   *
   * @param machine machine name
   * @param image docker image
   */
  void startWorkspaceMachine(String machine, String image);

  /**
   * Displays machine in running state.
   *
   * @param machine machine name
   */
  void onMachineRunning(String machine);

  /** Step 3. Initializing workspace agents. */
  void showInitializingWorkspaceAgents();

  /** Step 4. Workspace started. */
  void onWorkspaceStarted();
}
