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

/** View displaying loading progress of the workspace */
public interface WorkspaceLoadingTrackerView extends IsWidget {

  /** Adds machine to be booted. */
  void addMachine(String machineName);

  /** Sets image name for machine. */
  void setMachineImageName(String machineName, String imageName);

  /** Sets starting state for machine. */
  void setMachineStarting(String machineName);

  /** Sets running state for machine. */
  void setMachineRunning(String machineName);

  /** Creates installer and adds it to the machine. */
  void addInstaller(
      String machineName, String installerId, String installerName, String installerDescription);

  /** Sets starting state for installer. */
  void setInstallerStarting(String machineName, String installerId);

  /** Sets running state for installer. */
  void setInstallerRunning(String machineName, String installerId);

  void showWorkspaceStarting();

  void showWorkspaceStarted();

  void showWorkspaceStopped();

  void showWorkspaceFailed(String error);
}
