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
import static org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage.Sources.GIT;

import com.google.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.che.selenium.core.client.TestGitHubRepository;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.SeleniumWebDriverHelper;
import org.eclipse.che.selenium.core.workspace.TestWorkspace;
import org.eclipse.che.selenium.core.workspace.TestWorkspaceProvider;
import org.eclipse.che.selenium.pageobject.Ide;
import org.eclipse.che.selenium.pageobject.ProjectExplorer;
import org.eclipse.che.selenium.pageobject.ToastLoader;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace;
import org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack;
import org.eclipse.che.selenium.pageobject.dashboard.ProjectSourcePage;
import org.eclipse.che.selenium.pageobject.dashboard.workspaces.Workspaces;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Andrey Chizhikov */
public class ImportMavenProjectFromGitTest {

  private final String WORKSPACE = generate("ImtMvnPrjGit", 4);
  private String testProjectName;

  @Inject private Dashboard dashboard;
  @Inject private ProjectExplorer explorer;
  @Inject private NewWorkspace newWorkspace;
  @Inject private ProjectSourcePage projectSourcePage;
  @Inject private SeleniumWebDriverHelper seleniumWebDriverHelper;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;
  @Inject private TestWorkspaceProvider testWorkspaceProvider;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private Workspaces workspaces;
  @Inject private Ide ide;
  @Inject private ToastLoader toastLoader;
  @Inject private TestGitHubRepository testRepo;

  // it is used to read workspace logs on test failure
  private TestWorkspace testWorkspace;

  @BeforeClass
  public void setUp() throws IOException {
    Path entryPath = Paths.get(getClass().getResource("/projects/guess-project").getPath());
    testRepo.addContent(entryPath);

    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE, defaultTestUser.getName());
  }

  @Test
  public void checkAbilityImportMavenProjectTest() {
    testProjectName = testRepo.getName();

    dashboard.waitDashboardToolbarTitle();
    dashboard.selectWorkspacesItemOnDashboard();

    workspaces.clickOnAddWorkspaceBtn();
    newWorkspace.waitToolbar();

    // we are selecting 'Java' stack from the 'All Stack' tab for compatibility with OSIO
    newWorkspace.selectStack(Stack.JAVA_MAVEN);
    newWorkspace.typeWorkspaceName(WORKSPACE);

    projectSourcePage.clickOnAddOrImportProjectButton();

    projectSourcePage.selectSourceTab(GIT);
    projectSourcePage.typeGitRepositoryLocation(testRepo.getHtmlUrl());
    projectSourcePage.clickOnAddProjectButton();

    newWorkspace.clickOnCreateButtonAndOpenInIDE();
    // store info about created workspace to make SeleniumTestHandler.captureTestWorkspaceLogs()
    // possible to read logs in case of test failure
    testWorkspace = testWorkspaceProvider.getWorkspace(WORKSPACE, defaultTestUser);

    seleniumWebDriverHelper.switchToIdeFrameAndWaitAvailability();

    toastLoader.waitToastLoaderAndClickStartButton();
    ide.waitOpenedWorkspaceIsReadyToUse();
    explorer.waitItem(testProjectName);
  }
}
