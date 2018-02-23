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
import static org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails.StateWorkspace.STOPPED;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import com.google.inject.Inject;
import org.eclipse.che.selenium.core.client.TestFactoryServiceClient;
import org.eclipse.che.selenium.pageobject.Loader;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.DashboardFactory;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.eclipse.che.selenium.pageobject.dashboard.factories.FactoryDetails;
import org.eclipse.che.selenium.pageobject.dashboard.factories.NewFactory;
import org.eclipse.che.selenium.pageobject.dashboard.factories.NewFactory.TabNames;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.WorkspaceDetails;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CreateFactoryTest {

  private static final String MINIMAL_TEMPLATE_FACTORY_NAME = generate("factory", 4);
  private static final String COMPLETE_TEMPLATE_FACTORY_NAME = generate("factory", 4);
  private static final String FACTORY_CREATED_FROM_WORKSPACE_NAME = generate("factory", 4);
  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String MIN_FACTORY_NAME = generate("", 3);
  private static final String MAX_FACTORY_NAME = generate("", 20);
  private static final String NAME_IS_TOO_SHORT = "The name has to be more than 3 characters long.";
  private static final String NAME_IS_TOO_LONG = "The name has to be less than 20 characters long.";

  @Inject private TestFactoryServiceClient factoryServiceClient;
  @Inject private DashboardFactory dashboardFactory;
  @Inject private ProjectSourcePage projectSourcePage;
  @Inject private FactoryDetails factoryDetails;
  @Inject private NewFactory newFactory;
  @Inject private Dashboard dashboard;
  @Inject private Loader loader;
  @Inject private NewWorkspace newWorkspace;
  @Inject private WorkspaceDetails workspaceDetails;
  @Inject private Workspaces workspaces;

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
  }

  @Test
  public void checkNewFactoryFromPage() {
    dashboardFactory.selectFactoriesOnNavBar();
    dashboardFactory.waitAllFactoriesPage();
    dashboardFactory.clickOnAddFactoryBtn();
    newFactory.waitToolbarTitle();

    newFactory.typeFactoryName(FACTORY_CREATED_FROM_WORKSPACE_NAME);

    newFactory.clickOnSourceTab(TabNames.WORKSPACE_TAB_ID);
    newFactory.clickOnWorkspaceFromList(WORKSPACE_NAME);

    newFactory.clickOnSourceTab(TabNames.GIT_TAB_ID);
    newFactory.waitGitUrlField();

    newFactory.clickOnSourceTab(TabNames.CONFIG_TAB_ID);
    newFactory.waitUploadFileButton();

    newFactory.clickOnSourceTab(TabNames.TEMPLATE_TAB_ID);
    newFactory.waitTemplateButtons();
  }

  @Test
  public void checkFactoryName() {
    dashboardFactory.selectFactoriesOnNavBar();
    dashboardFactory.waitAllFactoriesPage();
    dashboardFactory.clickOnAddFactoryBtn();
    newFactory.waitToolbarTitle();

    newFactory.clickOnWorkspaceFromList(WORKSPACE_NAME);

    newFactory.typeFactoryName(MIN_FACTORY_NAME);
    newFactory.waitErrorMessageNotVisible();
    assertTrue(newFactory.isCreateButtonEnabled());

    newFactory.typeFactoryName(MAX_FACTORY_NAME);
    newFactory.waitErrorMessageNotVisible();
    assertTrue(newFactory.isCreateButtonEnabled());

    newFactory.typeFactoryName(generate("", 2));
    assertEquals(newFactory.getErrorMessage(), NAME_IS_TOO_SHORT);
    // Assert.assertFalse(newFactory.isCreateButtonEnabled());

    newFactory.typeFactoryName(generate("", 21));
    assertEquals(newFactory.getErrorMessage(), NAME_IS_TOO_LONG);
    // Assert.assertFalse(newFactory.isCreateButtonEnabled());
  }

  @Test
  public void createFactoryFromTemplates() {
    dashboardFactory.selectFactoriesOnNavBar();
    dashboardFactory.waitAllFactoriesPage();
    dashboardFactory.clickOnAddFactoryBtn();

    // create a factory from minimal template
    newFactory.waitToolbarTitle();
    newFactory.typeFactoryName(MINIMAL_TEMPLATE_FACTORY_NAME);
    newFactory.clickOnSourceTab(TabNames.TEMPLATE_TAB_ID);
    newFactory.waitTemplateButtons();
    newFactory.clickOnMinimalTemplateButton();
    newFactory.clickOnCreateButton();
    factoryDetails.waitFactoryName(MINIMAL_TEMPLATE_FACTORY_NAME);
    factoryDetails.clickOnBackToFactoriesListButton();

    dashboardFactory.waitAllFactoriesPage();
    dashboardFactory.waitFactoryName(MINIMAL_TEMPLATE_FACTORY_NAME);
    assertEquals(dashboardFactory.getFactoryRamLimit(MINIMAL_TEMPLATE_FACTORY_NAME), "2048 MB");

    // create a factory from complete template
    dashboardFactory.waitAllFactoriesPage();
    dashboardFactory.clickOnAddFactoryBtn();
    newFactory.waitToolbarTitle();
    newFactory.typeFactoryName(COMPLETE_TEMPLATE_FACTORY_NAME);
    newFactory.clickOnSourceTab(TabNames.TEMPLATE_TAB_ID);
    newFactory.waitTemplateButtons();
    newFactory.clickOnCompleteTemplateButton();
    newFactory.clickOnCreateButton();
    factoryDetails.waitFactoryName(COMPLETE_TEMPLATE_FACTORY_NAME);
    factoryDetails.clickOnBackToFactoriesListButton();

    dashboardFactory.waitAllFactoriesPage();
    dashboardFactory.waitFactoryName(COMPLETE_TEMPLATE_FACTORY_NAME);
    Assert.assertEquals(
        dashboardFactory.getFactoryRamLimit(COMPLETE_TEMPLATE_FACTORY_NAME), "2048 MB");
  }

  @Test
  public void createFactoryFromWorkspace() {
    dashboardFactory.selectFactoriesOnNavBar();
    dashboardFactory.waitAllFactoriesPage();
    dashboardFactory.clickOnAddFactoryBtn();

    // create a new factory from a workspace
    newFactory.clickOnSourceTab(TabNames.WORKSPACE_TAB_ID);
    newFactory.typeFactoryName(FACTORY_CREATED_FROM_WORKSPACE_NAME);
    newFactory.clickOnWorkspaceFromList(WORKSPACE_NAME);
    newFactory.clickOnCreateButton();
    factoryDetails.waitFactoryName(FACTORY_CREATED_FROM_WORKSPACE_NAME);
    factoryDetails.clickOnBackToFactoriesListButton();

    // check that the created factory exists
    dashboardFactory.waitAllFactoriesPage();
    dashboardFactory.waitFactoryName(FACTORY_CREATED_FROM_WORKSPACE_NAME);
    assertEquals(
        dashboardFactory.getFactoryRamLimit(FACTORY_CREATED_FROM_WORKSPACE_NAME), "3072 MB");
  }

  @Test
  public void checkWorkspaceFiltering() {
    dashboardFactory.selectFactoriesOnNavBar();
    dashboardFactory.waitAllFactoriesPage();
    dashboardFactory.clickOnAddFactoryBtn();

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
    newFactory.typeTextToSearchFactoryField(WORKSPACE_NAME.replace("r", "k"));
    newFactory.waitWorkspacesListIsEmpty();
  }

  private void createWorkspaceWithProject(String workspaceName) {
    String machineName = "dev-machine";

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
