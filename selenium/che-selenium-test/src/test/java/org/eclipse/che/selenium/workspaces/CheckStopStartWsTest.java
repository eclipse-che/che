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
package org.eclipse.che.selenium.workspaces;

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.STOP_WORKSPACE;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.eclipse.che.selenium.core.constant.TestWorkspaceConstants.RUNNING_WORKSPACE_MESS;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class CheckStopStartWsTest {
  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Loader loader;
  @Inject private MachineTerminal terminal;
  @Inject private ToastLoader toastLoader;
  @Inject private Menu menu;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(testWorkspace);
  }

  @Test
  public void checkStopStartWorkspaceTest() {
    projectExplorer.waitProjectExplorer();
    loader.waitOnClosed();
    menu.runCommand(WORKSPACE, STOP_WORKSPACE);
    toastLoader.waitExpectedTextInToastLoader("Snapshotting the workspace");
    toastLoader.waitExpectedTextInToastLoader("Workspace is not running", 60);
    toastLoader.clickOnStartButton();
    loader.waitOnClosed();
    toastLoader.waitExpectedTextInToastLoader("Starting workspace runtime.", 20);
    notificationsPopupPanel.waitExpectedMessageOnProgressPanelAndClosed(
        RUNNING_WORKSPACE_MESS, 120);
    loader.waitOnClosed();
    terminal.waitTerminalConsole(20);
  }
}
