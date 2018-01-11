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

import static org.eclipse.che.selenium.core.constant.TestBuildConstants.BUILD_SUCCESS;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.CREATE_PROJECT;
import static org.eclipse.che.selenium.core.constant.TestMenuCommandsConstants.Workspace.WORKSPACE;
import static org.eclipse.che.selenium.pageobject.Wizard.SamplesName.WEB_JAVA_SPRING;
import static org.eclipse.che.selenium.pageobject.dashboard.DashboardFactory.AddAction.RUN_COMMAND;

import com.google.inject.Inject;
import java.util.concurrent.ExecutionException;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestFactoryServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.pageobject.CodenvyEditor;
import org.eclipse.che.selenium.pageobject.Consoles;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.LoadingBehaviorPage;
import org.eclipse.che.selenium.pageobject.Menu;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.Wizard;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class CheckRunCommandFeatureTest {
  private static final String PROJECT_NAME = CheckRunCommandFeatureTest.class.getSimpleName();
  private static final String NAME_BUILD_COMMAND = PROJECT_NAME + ": build and run";
  private static final String FACTORY_NAME = NameGenerator.generate("factory", 4);

  @Inject private ProjectExplorer projectExplorer;
  @Inject private Dashboard dashboard;
  @Inject private DashboardFactory dashboardFactory;
  @Inject private Ide ide;
  @Inject private LoadingBehaviorPage loadingBehaviorPage;
  @Inject private CodenvyEditor editor;
  @Inject private Loader loader;
  @Inject private Wizard wizard;
  @Inject private Menu menu;
  @Inject private TestWorkspace testWorkspace;
  @Inject private TestUser user;
  @Inject private Consoles consoles;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TestFactoryServiceClient factoryServiceClient;

  @BeforeClass
  public void setUp() throws Exception {
    ide.open(testWorkspace);
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.deleteFactoryWorkspaces(testWorkspace.getName(), user.getName());
    factoryServiceClient.deleteFactory(FACTORY_NAME);
  }

  @Test
  public void checkRunCommandFeatureTest() throws ExecutionException, InterruptedException {
    createProject(PROJECT_NAME);
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(PROJECT_NAME);
    dashboard.open();
    dashboard.selectFactoriesOnDashbord();
    dashboardFactory.clickOnAddFactoryBtn();
    dashboardFactory.selectWorkspaceForCreation(testWorkspace.getName());
    dashboardFactory.setFactoryName(FACTORY_NAME);
    dashboardFactory.clickOnCreateFactoryBtn();
    dashboardFactory.selectAction(RUN_COMMAND);
    dashboardFactory.enterParamValue(NAME_BUILD_COMMAND);
    dashboardFactory.clickAddOnAddAction();
    dashboardFactory.clickOnOpenFactory();
    String currentWin = seleniumWebDriver.getWindowHandle();
    seleniumWebDriver.switchToNoneCurrentWindow(currentWin);
    loadingBehaviorPage.waitWhileLoadPageIsClosed();
    seleniumWebDriver.switchFromDashboardIframeToIde();
    projectExplorer.waitItem(PROJECT_NAME);
    consoles.waitExpectedTextIntoConsole(BUILD_SUCCESS);
  }

  private void createProject(String projectName) {
    projectExplorer.waitProjectExplorer();
    loader.waitOnClosed();
    menu.runCommand(WORKSPACE, CREATE_PROJECT);
    wizard.waitCreateProjectWizardForm();
    wizard.typeProjectNameOnWizard(projectName);
    wizard.selectSample(WEB_JAVA_SPRING);
    wizard.clickCreateButton();
    loader.waitOnClosed();
    wizard.waitCloseProjectConfigForm();
    loader.waitOnClosed();
    projectExplorer.waitProjectExplorer();
    projectExplorer.waitItem(projectName);
    loader.waitOnClosed();
  }
}
