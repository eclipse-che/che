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
package org.eclipse.che.selenium.factory;

import static org.eclipse.che.selenium.core.constant.TestGitConstants.CONFIGURING_PROJECT_AND_CLONING_SOURCE_CODE;
import static org.eclipse.che.selenium.pageobject.dashboard.DashboardFactory.SourcesTypes.WORKSPACES;

import com.google.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.LoadingBehaviorPage;
import org.eclipse.che.selenium.pageobject.MavenPluginStatusBar;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class CreateNamedFactoryFromDashBoard {
  private static final String PROJECT_NAME = CreateNamedFactoryFromDashBoard.class.getSimpleName();

  private String factoryWsName;

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private DefaultTestUser user;
  @Inject private DashboardFactory dashboardFactory;
  @Inject private Dashboard dashboard;
  @Inject private LoadingBehaviorPage loadingBehaviorPage;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Events events;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private MavenPluginStatusBar mavenPluginStatusBar;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Wizard wizard;
  @Inject private Menu menu;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(testWorkspace);
    menu.runCommand(
        TestMenuCommandsConstants.Workspace.WORKSPACE,
        TestMenuCommandsConstants.Workspace.CREATE_PROJECT);
    wizard.selectProjectAndCreate(Wizard.SamplesName.WEB_JAVA_SPRING, PROJECT_NAME);
  }

  @AfterClass
  public void tearDown() throws Exception {
    if (factoryWsName != null) {
      workspaceServiceClient.delete(factoryWsName, user.getName());
    }
  }

  @Test
  public void createFactoryFromDashBoard() throws ExecutionException, InterruptedException {
    String currentWin = ide.driver().getWindowHandle();
    dashboard.open();
    dashboardFactory.selectFactoryOnNavBar();
    dashboardFactory.waitAllFactoriesPage();
    dashboardFactory.clickOnAddFactoryBtn();
    dashboardFactory.waitSelectSourceWidgetAndSelect(WORKSPACES.toString());

    factoryWsName = testWorkspace.getName() + "_new";
    dashboardFactory.selectWorkspaceForCreation(factoryWsName);

    dashboardFactory.clickOnCreateFactoryBtn();
    dashboardFactory.waitJsonFactoryIsNotEmpty();
    dashboardFactory.setNameFactory(new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss").format(new Date()));
    dashboard.waitNotificationIsClosed();
    dashboardFactory.clickFactoryIDUrl();
    seleniumWebDriver.switchToNoneCurrentWindow(currentWin);
    loadingBehaviorPage.waitWhileLoadPageIsClosed();
    seleniumWebDriver.switchFromDashboardIframeToIde();
    projectExplorer.waitItem(PROJECT_NAME);
    events.clickProjectEventsTab();
    events.waitExpectedMessage(CONFIGURING_PROJECT_AND_CLONING_SOURCE_CODE);
    events.waitExpectedMessage("Project " + PROJECT_NAME + " imported");
    notificationsPopupPanel.waitPopUpPanelsIsClosed();
    projectExplorer.openItemByPath(PROJECT_NAME);
    mavenPluginStatusBar.waitClosingInfoPanel();
  }
}
