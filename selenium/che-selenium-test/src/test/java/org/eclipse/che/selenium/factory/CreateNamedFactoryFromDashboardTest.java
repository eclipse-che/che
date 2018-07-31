/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.factory;

import static org.eclipse.che.selenium.core.constant.TestGitConstants.CONFIGURING_PROJECT_AND_CLONING_SOURCE_CODE;

import com.google.inject.Inject;
import java.util.concurrent.ExecutionException;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestFactoryServiceClient;
import org.eclipse.che.selenium.core.client.TestProjectServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.Events;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.LoadingBehaviorPage;
import org.eclipse.che.selenium.pageobject.MavenPluginStatusBar;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.NotificationsPopupPanel;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.PullRequestPanel;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardFactories;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Musienko Maxim */
public class CreateNamedFactoryFromDashboardTest {
  private static final String PROJECT_NAME =
      CreateNamedFactoryFromDashboardTest.class.getSimpleName();
  private static final String FACTORY_NAME = NameGenerator.generate("factory", 4);

  @Inject private TestWorkspace testWorkspace;
  @Inject private Ide ide;
  @Inject private DefaultTestUser user;
  @Inject private DashboardFactories dashboardFactories;
  @Inject private Dashboard dashboard;
  @Inject private LoadingBehaviorPage loadingBehaviorPage;
  @Inject private ProjectExplorer projectExplorer;
  @Inject private Events events;
  @Inject private NotificationsPopupPanel notificationsPopupPanel;
  @Inject private MavenPluginStatusBar mavenPluginStatusBar;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private TestProjectServiceClient testProjectServiceClient;
  @Inject private Wizard wizard;
  @Inject private Menu menu;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TestFactoryServiceClient factoryServiceClient;
  @Inject private PullRequestPanel pullRequestPanel;

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
    dashboardFactories.selectFactoriesOnNavBar();
    dashboardFactories.waitAllFactoriesPage();
    dashboardFactories.clickOnAddFactoryBtn();
    dashboardFactories.selectWorkspaceForCreation(testWorkspace.getName());
    dashboardFactories.setFactoryName(FACTORY_NAME);
    dashboardFactories.clickOnCreateFactoryBtn();
    dashboardFactories.waitJsonFactoryIsNotEmpty();
    dashboard.waitNotificationIsClosed();
    dashboardFactories.clickFactoryIDUrl();
    seleniumWebDriverHelper.switchToNextWindow(currentWin);
    loadingBehaviorPage.waitWhileLoadPageIsClosed();
    seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();
    projectExplorer.waitItem(PROJECT_NAME);
    events.clickEventLogBtn();
    events.waitExpectedMessage(CONFIGURING_PROJECT_AND_CLONING_SOURCE_CODE);
    events.waitExpectedMessage("Project " + PROJECT_NAME + " imported");
    notificationsPopupPanel.waitPopupPanelsAreClosed();
    projectExplorer.waitAndSelectItem(PROJECT_NAME);
    pullRequestPanel.waitOpenPanel();
    projectExplorer.openItemByPath(PROJECT_NAME);
    mavenPluginStatusBar.waitClosingInfoPanel();
  }
}
