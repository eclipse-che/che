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
package org.eclipse.che.selenium.workspaces;

import static org.eclipse.che.selenium.core.constant.TestStacksConstants.JAVA;
import static org.eclipse.che.selenium.core.constant.TestTimeoutsConstants.EXPECTED_MESS_IN_CONSOLE_SEC;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.eclipse.che.selenium.pageobject.machineperspective.MachineTerminal;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class CreateWorkspaceOnDashboardTest {
  private static final String WORKSPACE = NameGenerator.generate("WsDashboard", 4);

  @Inject private CreateWorkspace createWorkspace;
  @Inject private TestUser defaultTestUser;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private MachineTerminal terminal;
  @Inject private Dashboard dashboard;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private Workspaces workspaces;

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE, defaultTestUser.getName());
  }

  @Test
  public void createWorkspaceOnDashboardTest() {
    dashboard.open();
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    dashboard.waitToolbarTitleName("Workspaces");

    workspaces.clickOnNewWorkspaceBtn();

    createWorkspace.waitToolbar();
    createWorkspace.typeWorkspaceName(WORKSPACE);
    createWorkspace.selectStack(JAVA.getId());
    createWorkspace.setMachineRAM("dev-machine", 2.0);
    createWorkspace.clickOnCreateButtonAndOpenInIDE();

    seleniumWebDriver.switchFromDashboardIframeToIde();

    projectExplorer.waitProjectExplorer();
    terminal.waitTerminalTab(EXPECTED_MESS_IN_CONSOLE_SEC);
  }
}
