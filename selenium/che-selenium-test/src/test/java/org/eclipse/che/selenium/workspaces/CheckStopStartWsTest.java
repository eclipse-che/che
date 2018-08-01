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
package org.eclipse.che.selenium.workspaces;

import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.STOP_WORKSPACE;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class CheckStopStartWsTest {
  @Inject private TestWorkspace testWorkspace;
  @Inject private ToastLoader toastLoader;
  @Inject private Menu menu;
  @Inject private Ide ide;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(testWorkspace);
  }

  @Test
  public void checkStopStartWorkspaceTest() {
    ide.waitOpenedWorkspaceIsReadyToUse();

    menu.runCommand(WORKSPACE, STOP_WORKSPACE);
    toastLoader.waitExpectedTextInToastLoader("Workspace is not running");

    toastLoader.clickOnToastLoaderButton("Start");
    ide.waitOpenedWorkspaceIsReadyToUse();
  }
}
