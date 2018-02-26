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
import static org.eclipse.che.selenium.pageobject.dashboard.factories.NewFactory.TabNames.CONFIG_TAB_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.factories.NewFactory.TabNames.GIT_TAB_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.factories.NewFactory.TabNames.TEMPLATE_TAB_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.factories.NewFactory.TabNames.WORKSPACE_TAB_ID;
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.StateWorkspace.STOPPED;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.client.TestFactoryServiceClient;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.TestUser;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardFactories;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.eclipse.che.selenium.pageobject.dashboard.factories.FactoryDetails;
import org.eclipse.che.selenium.pageobject.dashboard.factories.NewFactory;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CreateFactoryTest {

  private static final String MINIMAL_TEMPLATE_FACTORY_NAME = generate("factory", 4);
  private static final String COMPLETE_TEMPLATE_FACTORY_NAME = generate("factory", 4);
  private static final String FACTORY_CREATED_FROM_WORKSPACE_NAME = generate("factory", 4);
  private static final String MIN_FACTORY_NAME = generate("", 3);
  private static final String MAX_FACTORY_NAME = generate("", 20);
  private static final String NAME_IS_TOO_SHORT = "The name has to be more than 3 characters long.";
  private static final String NAME_IS_TOO_LONG = "The name has to be less than 20 characters long.";
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
  @Inject private NewFactory newFactory;
  @Inject private Dashboard dashboard;
  @Inject private Loader loader;

  @BeforeClass
  public void setUp() throws Exception {
    dashboard.open();
    createWorkspaceWithProject(WORKSPACE_NAME);
  }

  @AfterClass
  public void tearDown() throws Exception {
    factoryServiceClient.deleteFactory(MINIMAL_TEMPLATE_FACTORY_NAME);
    factoryServiceClient.deleteFactory(COMPLETE_TEMPLATE_FACTORY_NAME);
    factoryServiceClient.deleteFactory(FACTORY_CREATED_FROM_WORKSPACE_NAME);
    workspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void checkNewFactoryPage() {
    dashboardFactories.selectFactoriesOnNavBar();
    dashboardFactories.waitAllFactoriesPage();
    dashboardFactories.clickOnAddFactoryBtn();
    newFactory.waitToolbarTitle();

    // open tabs and check it fields
    newFactory.clickOnSourceTab(WORKSPACE_TAB_ID);
    assertTrue(newFactory.isCreateFactoryButtonDisabled());
    newFactory.clickOnWorkspaceFromList(WORKSPACE_NAME);
    assertFalse(newFactory.isCreateFactoryButtonDisabled());

    newFactory.clickOnSourceTab(GIT_TAB_ID);
    newFactory.waitGitUrlField();

    newFactory.clickOnSourceTab(CONFIG_TAB_ID);
    newFactory.waitUploadFileButton();

    newFactory.clickOnSourceTab(TEMPLATE_TAB_ID);
    newFactory.waitTemplateButtons();
  }

  @Test
  public void checkFactoryName() {
    dashboardFactories.selectFactoriesOnNavBar();
    dashboardFactories.waitAllFactoriesPage();
    dashboardFactories.clickOnAddFactoryBtn();
    newFactory.waitToolbarTitle();

    newFactory.clickOnWorkspaceFromList(WORKSPACE_NAME);

    // type valid factory names and check that the Create button is enabled
    newFactory.typeFactoryName(MIN_FACTORY_NAME);
    newFactory.waitErrorMessageNotVisible();
    assertFalse(newFactory.isCreateFactoryButtonDisabled());
    newFactory.typeFactoryName(MAX_FACTORY_NAME);
    newFactory.waitErrorMessageNotVisible();
    assertFalse(newFactory.isCreateFactoryButtonDisabled());

    // type incorrect factory names and check error messages
    newFactory.typeFactoryName(generate("", 2));
    assertEquals(newFactory.getErrorMessage(), NAME_IS_TOO_SHORT);
    newFactory.typeFactoryName(generate("", 21));
    assertEquals(newFactory.getErrorMessage(), NAME_IS_TOO_LONG);
  }

  @Test
  public void checkCreatingFactoryFromTemplates() {
    dashboardFactories.selectFactoriesOnNavBar();
    dashboardFactories.waitAllFactoriesPage();
    dashboardFactories.clickOnAddFactoryBtn();

    // create a factory from minimal template
    newFactory.waitToolbarTitle();
    newFactory.typeFactoryName(MINIMAL_TEMPLATE_FACTORY_NAME);
    newFactory.clickOnSourceTab(TEMPLATE_TAB_ID);
    newFactory.waitTemplateButtons();
    newFactory.clickOnMinimalTemplateButton();
    newFactory.clickOnCreateFactoryButton();
    factoryDetails.waitFactoryName(MINIMAL_TEMPLATE_FACTORY_NAME);
    factoryDetails.clickOnBackToFactoriesListButton();

    dashboardFactories.waitAllFactoriesPage();
    dashboardFactories.waitFactoryName(MINIMAL_TEMPLATE_FACTORY_NAME);
    assertEquals(dashboardFactories.getFactoryRamLimit(MINIMAL_TEMPLATE_FACTORY_NAME), "2048 MB");

    // create a factory from complete template
    dashboardFactories.waitAllFactoriesPage();
    dashboardFactories.clickOnAddFactoryBtn();
    newFactory.waitToolbarTitle();
    newFactory.typeFactoryName(COMPLETE_TEMPLATE_FACTORY_NAME);
    newFactory.clickOnSourceTab(TEMPLATE_TAB_ID);
    newFactory.waitTemplateButtons();
    newFactory.clickOnCompleteTemplateButton();
    newFactory.clickOnCreateFactoryButton();
    factoryDetails.waitFactoryName(COMPLETE_TEMPLATE_FACTORY_NAME);
    factoryDetails.clickOnBackToFactoriesListButton();

    dashboardFactories.waitAllFactoriesPage();
    dashboardFactories.waitFactoryName(COMPLETE_TEMPLATE_FACTORY_NAME);
    assertEquals(dashboardFactories.getFactoryRamLimit(COMPLETE_TEMPLATE_FACTORY_NAME), "2048 MB");
  }

  @Test
  public void checkCreatingFactoryFromWorkspace() {
    dashboardFactories.selectFactoriesOnNavBar();
    dashboardFactories.waitAllFactoriesPage();
    dashboardFactories.clickOnAddFactoryBtn();

    // create a new factory from a workspace
    newFactory.clickOnSourceTab(WORKSPACE_TAB_ID);
    newFactory.typeFactoryName(FACTORY_CREATED_FROM_WORKSPACE_NAME);
    newFactory.clickOnWorkspaceFromList(WORKSPACE_NAME);
    newFactory.clickOnCreateFactoryButton();
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
    dashboardFactories.selectFactoriesOnNavBar();
    dashboardFactories.waitAllFactoriesPage();
    dashboardFactories.clickOnAddFactoryBtn();

    newFactory.waitToolbarTitle();
    newFactory.clickOnSearchFactoryButton();
    newFactory.waitSearchFactoryField();

    // filter by full workspace name
    newFactory.typeTextToSearchFactoryField(WORKSPACE_NAME);
    newFactory.waitWorkspaceNameInList(WORKSPACE_NAME);

    // filter by a part of workspace name
    newFactory.typeTextToSearchFactoryField(WORKSPACE_NAME.substring(WORKSPACE_NAME.length() / 2));
    newFactory.waitWorkspaceNameInList(WORKSPACE_NAME);

    // filter by a nonexistent workspace name
    newFactory.typeTextToSearchFactoryField(generate("", 8));
    newFactory.waitWorkspacesListIsEmpty();
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
