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
package org.eclipse.che.selenium.factory;

import static org.eclipse.che.selenium.core.constant.TestGitConstants.CONFIGURING_PROJECT_AND_CLONING_SOURCE_CODE;
import static org.testng.Assert.fail;

import com.google.inject.Inject;
import java.util.concurrent.ExecutionException;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestFactoryServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.user.TestUser;
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
import org.openqa.selenium.TimeoutException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class CreateNamedFactoryFromDashBoard {
  private static final String PROJECT_NAME = CreateNamedFactoryFromDashBoard.class.getSimpleName();
  private static final String FACTORY_NAME = NameGenerator.generate("factory", 4);

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private TestUser user;
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
  @Inject private TestFactoryServiceClient factoryServiceClient;

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
    workspaceServiceClient.deleteFactoryWorkspaces(testWorkspace.getName(), user.getName());
    factoryServiceClient.deleteFactory(FACTORY_NAME);
  }

  @Test
  public void createFactoryFromDashBoard() throws ExecutionException, InterruptedException {
    String currentWin = seleniumWebDriver.getWindowHandle();
    dashboard.open();
    dashboardFactory.selectFactoryOnNavBar();
    dashboardFactory.waitAllFactoriesPage();
    dashboardFactory.clickOnAddFactoryBtn();
    dashboardFactory.selectWorkspaceForCreation(testWorkspace.getName());
    dashboardFactory.setFactoryName(FACTORY_NAME);
    dashboardFactory.clickOnCreateFactoryBtn();
    dashboardFactory.waitJsonFactoryIsNotEmpty();
    dashboard.waitNotificationIsClosed();
    dashboardFactory.clickFactoryIDUrl();
    seleniumWebDriver.switchToNoneCurrentWindow(currentWin);
    loadingBehaviorPage.waitWhileLoadPageIsClosed();
    seleniumWebDriver.switchFromDashboardIframeToIde();
    projectExplorer.waitItem(PROJECT_NAME);
    events.clickEventLogBtn();
    events.waitExpectedMessage(CONFIGURING_PROJECT_AND_CLONING_SOURCE_CODE);
    events.waitExpectedMessage("Project " + PROJECT_NAME + " imported");
    notificationsPopupPanel.waitPopUpPanelsIsClosed();

    try {
      projectExplorer.openItemByPath(PROJECT_NAME);
    } catch (TimeoutException ex) {
      // remove try-catch block after issue has been resolved
      fail("Known issue https://github.com/eclipse/che/issues/7959");
    }

    mavenPluginStatusBar.waitClosingInfoPanel();
  }
}
