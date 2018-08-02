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

  /** Shows workspace starting section. */
  void showWorkspaceStarting();

  /** Shows workspace started section. */
  void showWorkspaceStarted();

  /** Shows workspace stopping section. */
  void showWorkspaceStopping();

  /** Shows workspace stopped section. */
  void showWorkspaceStopped();

  /** Marks machine failed. */
  void setMachineFailed(String machineName);

  /** Marks installer failed and displays corresponding error message. */
  void setInstallerFailed(String machineName, String installerId, String errorMessage);

  /** Shows workspace failed section. */
  void showWorkspaceFailed(String error);

  /** Sets action delegate. */
  void setDelegate(ActionDelegate delegate);

  interface ActionDelegate {

    /** On show output for the machine. */
    void onShowMachineOutputs(String machineName);
  }
}
