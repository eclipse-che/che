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
package org.eclipse.che.selenium.dashboard;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestStacksConstants.JAVA;
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Template.WEB_JAVA_SPRING;
import static org.eclipse.che.selenium.pageobject.dashboard.factories.CreateFactoryPage.TabNames.CONFIG_TAB_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.factories.CreateFactoryPage.TabNames.GIT_TAB_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.factories.CreateFactoryPage.TabNames.TEMPLATE_TAB_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.factories.CreateFactoryPage.TabNames.WORKSPACE_TAB_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.StateWorkspace.STOPPED;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.client.TestFactoryServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardFactories;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.eclipse.che.selenium.pageobject.dashboard.factories.CreateFactoryPage;
import org.eclipse.che.selenium.pageobject.dashboard.factories.FactoryDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CreateFactoryTest {

  private static final String MINIMAL_TEMPLATE_FACTORY_NAME = generate("factory", 4);
  private static final String COMPLETE_TEMPLATE_FACTORY_NAME = generate("factory", 4);
  private static final String FACTORY_CREATED_FROM_WORKSPACE_NAME = generate("factory", 4);
  private static final String MIN_FACTORY_NAME = generate("", 3);
  private static final String MAX_FACTORY_NAME = generate("", 20);
  private static final String TOO_SHORT_NAME = "The name has to be more than 3 characters long.";
  private static final String TOO_LONG_NAME = "The name has to be less than 20 characters long.";
  private static final String WORKSPACE_NAME = generate("workspace", 4);

  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TestFactoryServiceClient factoryServiceClient;
  @Inject private DashboardFactories dashboardFactories;
  @Inject private ProjectSourcePage projectSourcePage;
  @Inject private WorkspaceDetails workspaceDetails;
  @Inject private FactoryDetails factoryDetails;
  @Inject private NewWorkspace newWorkspace;
  @Inject private TestUser defaultTestUser;
  @Inject private Workspaces workspaces;
  @Inject private CreateFactoryPage createFactoryPage;
  @Inject private Dashboard dashboard;
  @Inject private Loader loader;

  @BeforeClass
  public void setUp() throws Exception {
    dashboard.open();
    createWorkspaceWithProject(WORKSPACE_NAME);
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
    factoryServiceClient.deleteFactory(MINIMAL_TEMPLATE_FACTORY_NAME);
    factoryServiceClient.deleteFactory(COMPLETE_TEMPLATE_FACTORY_NAME);
    factoryServiceClient.deleteFactory(FACTORY_CREATED_FROM_WORKSPACE_NAME);
  }

  @BeforeMethod
  private void openNewFactoryPage() {
    // open the New Factory page before starting each test method
    dashboardFactories.selectFactoriesOnNavBar();
    dashboardFactories.waitAllFactoriesPage();
    dashboardFactories.clickOnAddFactoryBtn();
    createFactoryPage.waitToolbarTitle();
  }

  @Test
  public void checkGitAndConfigTabs() {
    // open tabs and check their fields
    createFactoryPage.clickOnSourceTab(GIT_TAB_ID);
    createFactoryPage.waitGitUrlField();

    createFactoryPage.clickOnSourceTab(CONFIG_TAB_ID);
    createFactoryPage.waitUploadFileButton();
  }

  @Test
  public void shouldHandleIncorrectFactoryNames() {
    // select created workspace from list of workspaces
    createFactoryPage.clickOnWorkspaceFromList(WORKSPACE_NAME);

    // type valid factory names and check that the Create button is enabled
    createFactoryPage.typeFactoryName(MIN_FACTORY_NAME);
    createFactoryPage.waitErrorMessageNotVisible();
    assertFalse(createFactoryPage.isCreateFactoryButtonDisabled());
    createFactoryPage.typeFactoryName(MAX_FACTORY_NAME);
    createFactoryPage.waitErrorMessageNotVisible();
    assertFalse(createFactoryPage.isCreateFactoryButtonDisabled());

    // type incorrect factory names and check error messages
    createFactoryPage.typeFactoryName(generate("", 2));
    assertEquals(createFactoryPage.getErrorMessage(), TOO_SHORT_NAME);
    createFactoryPage.typeFactoryName(generate("", 21));
    assertEquals(createFactoryPage.getErrorMessage(), TOO_LONG_NAME);
  }

  @Test
  public void shouldCreateFactoryFromTemplate() {
    // create a factory from minimal template
    createFactoryPage.waitToolbarTitle();
    createFactoryPage.typeFactoryName(MINIMAL_TEMPLATE_FACTORY_NAME);
    createFactoryPage.clickOnSourceTab(TEMPLATE_TAB_ID);
    createFactoryPage.waitTemplateButtons();
    createFactoryPage.clickOnMinimalTemplateButton();
    createFactoryPage.clickOnCreateFactoryButton();
    factoryDetails.waitFactoryName(MINIMAL_TEMPLATE_FACTORY_NAME);
    factoryDetails.clickOnBackToFactoriesListButton();

    dashboardFactories.waitAllFactoriesPage();
    dashboardFactories.waitFactoryName(MINIMAL_TEMPLATE_FACTORY_NAME);
    assertEquals(dashboardFactories.getFactoryRamLimit(MINIMAL_TEMPLATE_FACTORY_NAME), "2048 MB");

    // create a factory from complete template
    dashboardFactories.waitAllFactoriesPage();
    dashboardFactories.clickOnAddFactoryBtn();
    createFactoryPage.waitToolbarTitle();
    createFactoryPage.typeFactoryName(COMPLETE_TEMPLATE_FACTORY_NAME);
    createFactoryPage.clickOnSourceTab(TEMPLATE_TAB_ID);
    createFactoryPage.waitTemplateButtons();
    createFactoryPage.clickOnCompleteTemplateButton();
    createFactoryPage.clickOnCreateFactoryButton();
    factoryDetails.waitFactoryName(COMPLETE_TEMPLATE_FACTORY_NAME);
    factoryDetails.clickOnBackToFactoriesListButton();

    dashboardFactories.waitAllFactoriesPage();
    dashboardFactories.waitFactoryName(COMPLETE_TEMPLATE_FACTORY_NAME);
    assertEquals(dashboardFactories.getFactoryRamLimit(COMPLETE_TEMPLATE_FACTORY_NAME), "2048 MB");
  }

  @Test
  public void shouldCreatingFactoryFromWorkspace() {
    // create a new factory from a workspace
    createFactoryPage.clickOnSourceTab(WORKSPACE_TAB_ID);
    createFactoryPage.typeFactoryName(FACTORY_CREATED_FROM_WORKSPACE_NAME);
    createFactoryPage.clickOnWorkspaceFromList(WORKSPACE_NAME);
    createFactoryPage.clickOnCreateFactoryButton();
    factoryDetails.waitFactoryName(FACTORY_CREATED_FROM_WORKSPACE_NAME);
    factoryDetails.clickOnBackToFactoriesListButton();

    // check that the created factory exists in the Factories list
    dashboardFactories.waitAllFactoriesPage();
    dashboardFactories.waitFactoryName(FACTORY_CREATED_FROM_WORKSPACE_NAME);
    assertEquals(
        dashboardFactories.getFactoryRamLimit(FACTORY_CREATED_FROM_WORKSPACE_NAME), "3072 MB");
  }

  @Test
  public void checkWorkspaceFiltering() {
    // click on the search button and wait search field visible
    createFactoryPage.clickOnSearchFactoryButton();
    createFactoryPage.waitSearchFactoryField();

    // filter by full workspace name
    createFactoryPage.typeTextToSearchFactoryField(WORKSPACE_NAME);
    createFactoryPage.waitWorkspaceNameInList(WORKSPACE_NAME);

    // filter by a part of workspace name
    createFactoryPage.typeTextToSearchFactoryField(
        WORKSPACE_NAME.substring(WORKSPACE_NAME.length() / 2));
    createFactoryPage.waitWorkspaceNameInList(WORKSPACE_NAME);

    // filter by a nonexistent workspace name
    createFactoryPage.typeTextToSearchFactoryField(generate("", 8));
    createFactoryPage.waitWorkspacesListIsEmpty();
  }

  private void createWorkspaceWithProject(String workspaceName) {
    String machineName = "dev-machine";

    // create a workspace from the Java stack with the web-java-spring project
    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    dashboard.waitToolbarTitleName("Workspaces");
    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitToolbar();
    loader.waitOnClosed();
    newWorkspace.selectStack(JAVA.getId());
    newWorkspace.typeWorkspaceName(workspaceName);
    newWorkspace.setMachineRAM(machineName, 3.0);

    projectSourcePage.clickOnAddOrImportProjectButton();
    projectSourcePage.selectSample(WEB_JAVA_SPRING);
    projectSourcePage.clickOnAddProjectButton();
    projectSourcePage.waitCreatedProjectButton(WEB_JAVA_SPRING);

    newWorkspace.clickOnCreateButtonAndEditWorkspace();

    workspaceDetails.waitToolbarTitleName(workspaceName);
    workspaceDetails.checkStateOfWorkspace(STOPPED);
  }
}
