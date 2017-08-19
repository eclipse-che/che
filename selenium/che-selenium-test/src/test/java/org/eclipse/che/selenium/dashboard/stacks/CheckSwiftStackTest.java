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
package org.eclipse.che.selenium.dashboard.stacks;

import com.google.inject.Inject;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.constant.TestStacksConstants;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.dashboard.CreateWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.NavigationBar;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class CheckSwiftStackTest {

  private static final String WS_NAME = NameGenerator.generate("workspace", 4);
  private static final String PROJECT_NAME = "swift";

  @Inject private DefaultTestUser user;
  @Inject private NavigationBar navigationBar;
  @Inject private CreateWorkspace createWorkspace;
  @Inject private Dashboard dashboard;
  @Inject private DashboardWorkspace dashboardWorkspace;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private ProjectSourcePage projectSourcePage;

  @BeforeClass
  public void setUp() throws Exception {
    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WS_NAME, user.getName());
  }

  @Test
  public void checkSwiftStack() throws Exception {
    navigationBar.waitNavigationBar();
    navigationBar.clickOnMenu(NavigationBar.MenuItem.WORKSPACES);

    dashboardWorkspace.clickOnNewWorkspaceBtn();
    createWorkspace.waitToolbar();
    createWorkspace.clickAllStacksTab();
    createWorkspace.selectStack(TestStacksConstants.BITNAMI_SWIFT.getId());
    createWorkspace.typeWorkspaceName(WS_NAME);
    projectSourcePage.clickAddOrImportProjectButton();
    projectSourcePage.selectSample(PROJECT_NAME);
    projectSourcePage.clickAdd();
    createWorkspace.clickCreate();

    seleniumWebDriver.switchFromDashboardIframeToIde();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME, 500);
    projectExplorer.waitFolderDefinedTypeOfFolderByPath(
        PROJECT_NAME, ProjectExplorer.FolderTypes.PROJECT_FOLDER);
  }
}
