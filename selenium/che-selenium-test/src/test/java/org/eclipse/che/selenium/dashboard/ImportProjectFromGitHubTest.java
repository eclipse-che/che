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
package org.eclipse.che.selenium.dashboard;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.utils.WaitUtils.sleepQuietly;
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Sources.GITHUB;
import static org.testng.AssertJUnit.assertTrue;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceProvider;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.eclipse.che.selenium.pageobject.theia.TheiaIde;
import org.eclipse.che.selenium.pageobject.theia.TheiaProjectTree;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ImportProjectFromGitHubTest {
  private static final String WORKSPACE =
      generate(ImportProjectFromGitHubTest.class.getSimpleName(), 4);

  private String projectName;
  private String testRepoName;
  private String ideWin;

  @Inject
  @Named("github.username")
  private String gitHubUsername;

  @Inject
  @Named("github.password")
  private String gitHubPassword;

  @Inject private Ide ide;
  @Inject private Dashboard dashboard;
  @Inject private Workspaces workspaces;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private NewWorkspace newWorkspace;
  @Inject private ProjectSourcePage projectSourcePage;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TestWorkspaceProvider testWorkspaceProvider;
  @Inject private TestGitHubRepository testRepo;
  @Inject private TheiaIde theiaIde;
  @Inject private TheiaProjectTree theiaProjectTree;

  // it is used to read workspace logs on test failure
  private TestWorkspace testWorkspace;

  @BeforeClass
  public void setUp() throws IOException {
    Path entryPath = Paths.get(getClass().getResource("/projects/testRepo").getPath());
    testRepo.addContent(entryPath);

    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE, defaultTestUser.getName());
  }

  @Test
  public void checkAbilityImportProjectFromGithub() {
    testRepoName = testRepo.getName();
    projectName = String.format("%s-%s", gitHubUsername, testRepoName);

    ideWin = seleniumWebDriver.getWindowHandle();

    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();
    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitToolbar();
    // we are selecting 'Java' stack from the 'All Stack' tab for compatibility with OSIO
    newWorkspace.selectStack(Stack.JAVA_MAVEN);
    newWorkspace.typeWorkspaceName(WORKSPACE);

    projectSourcePage.clickOnAddOrImportProjectButton();
    projectSourcePage.selectSourceTab(GITHUB);

    if (projectSourcePage.isConnectGitHubAccountButtonVisible()) {
      connectGithubAccount();
    }

    assertTrue(projectSourcePage.isGithubProjectsListDisplayed());
    projectSourcePage.selectProjectFromList(testRepoName);
    projectSourcePage.clickOnAddProjectButton();
    newWorkspace.clickOnCreateButtonAndOpenInIDE();
    // store info about created workspace to make SeleniumTestHandler.captureTestWorkspaceLogs()
    // possible to read logs in case of test failure
    testWorkspace = testWorkspaceProvider.getWorkspace(WORKSPACE, defaultTestUser);

    theiaIde.switchToIdeFrame();
    theiaIde.waitTheiaIde();
    theiaIde.waitLoaderInvisibility();
    theiaIde.waitTheiaIdeTopPanel();

    // wait the project in the tree
    theiaProjectTree.clickOnFilesTab();
    theiaProjectTree.waitProjectsRootItem();
    theiaProjectTree.waitProjectAreaOpened();
    theiaProjectTree.waitItem(projectName);
  }

  private void connectGithubAccount() {
    projectSourcePage.clickOnConnectGithubAccountButton();

    seleniumWebDriverHelper.waitOpenedSomeWin();
    seleniumWebDriverHelper.switchToNextWindow(ideWin);

    // workaround to avoid freezing blank popup window in chrome-node 3.141.59-dubnium
    sleepQuietly(5);

    projectSourcePage.waitAuthorizationPageOpened();

    projectSourcePage.typeLogin(gitHubUsername);
    projectSourcePage.typePassword(gitHubPassword);
    projectSourcePage.clickOnSignInButton();
    seleniumWebDriver.switchTo().window(ideWin);

    if (!projectSourcePage.isGithubProjectsListDisplayed()) {
      seleniumWebDriverHelper.switchToNextWindow(ideWin);

      projectSourcePage.waitAuthorizeBtn();
      projectSourcePage.clickOnAuthorizeBtn();
      seleniumWebDriver.switchTo().window(ideWin);
    }
  }
}
